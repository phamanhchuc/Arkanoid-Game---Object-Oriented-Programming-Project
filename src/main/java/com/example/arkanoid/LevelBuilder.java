package com.example.arkanoid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Lớp này sẽ chịu trách nhiệm xây dựng cấu trúc gạch cho một cấp độ
public class LevelBuilder {

    private final int playAreaWidth;
    private final double playAreaOffsetX;
    private final Map<Integer, BrickFactory> brickFactories;

    // Constructor nhận các thông tin cần thiết từ GameManager
    public LevelBuilder(int playAreaWidth, double playAreaOffsetX, Map<Integer, BrickFactory> brickFactories) {
        this.playAreaWidth = playAreaWidth;
        this.playAreaOffsetX = playAreaOffsetX;
        this.brickFactories = brickFactories;
    }

    /**
     * Tải và tạo tất cả các viên gạch (tĩnh và di chuyển) cho một cấp độ.
     * @param levelFileName Tên file map của cấp độ.
     * @param allBricks Danh sách để thêm tất cả các viên gạch vào (tĩnh và từ hàng di chuyển).
     * @param movingBrickRows Danh sách để thêm các MovingBrickRow vào.
     * @return true nếu tải thành công, false nếu có lỗi.
     */
    public boolean buildLevelBricks(String levelFileName, List<Brick> allBricks, List<MovingBrickRow> movingBrickRows) {
        allBricks.clear();
        movingBrickRows.clear();

        String path = "/com/example/arkanoid/levels/" + levelFileName;
        List<String[]> mapData = new ArrayList<>(); // Dữ liệu cho gạch tĩnh
        int cols = 0;

        try (InputStream is = getClass().getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            if (is == null) {
                System.err.println("Lỗi nghiêm trọng: Không tìm thấy file map: " + path);
                return false;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Bỏ qua dòng trống
                }

                // --- LOGIC PHÂN TÍCH DÒNG MAP ---
                if (line.startsWith("#ROW")) {
                    // Đây là một định nghĩa MovingBrickRow
                    parseAndCreateMovingBrickRow(line, allBricks, movingBrickRows);
                } else {
                    // Đây là dữ liệu gạch tĩnh bình thường
                    String[] numbers = line.split("\\s+");
                    mapData.add(numbers);
                    if (cols == 0) { cols = numbers.length; }
                }
            }
        } catch (IOException | NullPointerException e) {
            System.err.println("Lỗi khi đọc file map: " + path);
            e.printStackTrace();
            return false;
        }

        // --- TẠO GẠCH TĨNH TỪ MAPDATA ---
        if (!mapData.isEmpty() && cols > 0) {
            int rows = mapData.size();
            double horizontalPadding = 50.0;
            double verticalPaddingTop = 60.0;
            double brickSpacingX = 0.0;
            double brickSpacingY = 0.0;
            double brickW = (playAreaWidth - horizontalPadding * 2 - (cols - 1) * brickSpacingX) / cols;
            double brickH = 22;

            for (int r = 0; r < rows; r++) {
                String[] numbers = mapData.get(r);
                for (int c = 0; c < numbers.length; c++) {
                    int brickType = 0;
                    try {
                        brickType = Integer.parseInt(numbers[c]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (brickFactories.containsKey(brickType)) {
                        BrickFactory factory = brickFactories.get(brickType);
                        double x = playAreaOffsetX + horizontalPadding + c * (brickW + brickSpacingX);
                        double y = verticalPaddingTop + r * (brickH + brickSpacingY);
                        Brick brick = factory.createBrick(x, y, brickW, brickH);
                        allBricks.add(brick);
                    }
                }
            }
        }

        System.out.println("Đã tải thành công map: " + levelFileName +
                " (gạch tĩnh: " + (mapData.isEmpty() ? 0 : mapData.size() + "x" + cols) +
                ", hàng di chuyển: " + movingBrickRows.size() + ")");
        return true;
    }


    // --- HÀM MỚI ĐỂ PHÂN TÍCH CÚ PHÁP VÀ TẠO MOVINGBRICKROW ---
    private void parseAndCreateMovingBrickRow(String line, List<Brick> allBricks, List<MovingBrickRow> movingBrickRows) {
        // Ví dụ: #ROW 5 5 300 70.0 20 780 300 300
        String[] parts = line.split("\\s+");

        if (parts.length != 9) {
            // Sửa Lỗi 2: In ra thông báo chính xác
            System.err.println("Lỗi định dạng dòng #ROW: " + line + ". Cần 9 tham số (1 chỉ định + 8 giá trị).");
            return; // Dừng lại nếu định dạng sai, không tạo MovingBrickRow
        }

        try {
            int numBricks = Integer.parseInt(parts[1]);
            int brickType = Integer.parseInt(parts[2]);
            double rowY = Double.parseDouble(parts[3]);
            double initialDx = Double.parseDouble(parts[4]);
            // Giới hạn đọc từ file là offset, cần cộng thêm playAreaOffsetX
            double limitLeft = playAreaOffsetX + Double.parseDouble(parts[5]);
            double limitRight = playAreaOffsetX + Double.parseDouble(parts[6]);
            double limitTop = Double.parseDouble(parts[7]);
            double limitBottom = Double.parseDouble(parts[8]);

            // Định nghĩa kích thước gạch mặc định và khoảng cách
            double BRICK_W = 60;
            double BRICK_H = 22;
            int brickSpacing = 10;

            // Tính toán vị trí X bắt đầu cho hàng gạch để căn giữa
            double totalRowWidth = (numBricks * BRICK_W) + ((numBricks - 1) * brickSpacing);
            double startXForRow = playAreaOffsetX + (playAreaWidth - totalRowWidth) / 2.0;

            // Tạo đối tượng MovingBrickRow
            MovingBrickRow newRow = new MovingBrickRow(
                    startXForRow, rowY,
                    totalRowWidth, BRICK_H,
                    initialDx, 0.0,      // initialDy = 0.0 mặc định cho hàng ngang
                    limitLeft, limitRight, limitTop, limitBottom
            );

            // Tạo các viên gạch và thêm vào MovingBrickRow
            for (int i = 0; i < numBricks; i++) {
                double currentBrickX = startXForRow + (i * (BRICK_W + brickSpacing));
                Brick brick = new Brick(currentBrickX, rowY, BRICK_W, BRICK_H, brickType);
                newRow.addBrick(brick);  // Thêm vào MovingBrickRow để nó quản lý
                allBricks.add(brick);    // Thêm vào danh sách bricks chung của game
            }
            movingBrickRows.add(newRow); // Thêm hàng vào danh sách các hàng di chuyển của game

        } catch (NumberFormatException e) {
            System.err.println("Lỗi chuyển đổi số trong dòng #ROW: " + line);
            e.printStackTrace();
        }
    }
}