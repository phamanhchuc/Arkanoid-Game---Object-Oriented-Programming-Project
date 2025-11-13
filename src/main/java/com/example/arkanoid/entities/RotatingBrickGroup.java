package com.example.arkanoid.entities;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

/**
 * Lớp này quản lý một nhóm các viên gạch xoay tròn đồng thời quanh một điểm trung tâm cố định.
 */
public class RotatingBrickGroup {

    private final List<Brick> bricksInGroup; // Danh sách các viên gạch trong nhóm
    private final double centerX;           // Tọa độ X của điểm trung tâm xoay
    private final double centerY;           // Tọa độ Y của điểm trung tâm xoay
    private final double rotationSpeed;     // Tốc độ xoay của cả nhóm (độ/giây, dương là ngược chiều kim đồng hồ)
    private final double radius;            // Bán kính của quỹ đạo xoay tròn của nhóm

    private double currentGroupAngle;       // Góc xoay hiện tại của toàn bộ nhóm (độ)

    /**
     * Constructor cho RotatingBrickGroup.
     *
     * @param bricks        Danh sách các viên gạch sẽ thuộc về nhóm này.
     *                      Các viên gạch này nên được tạo ở vị trí ban đầu trên vòng tròn.
     * @param centerX       Tọa độ X của điểm trung tâm xoay.
     * @param centerY       Tọa độ Y của điểm trung tâm xoay.
     * @param radius        Bán kính của vòng tròn xoay.
     * @param rotationSpeed Tốc độ xoay của nhóm (độ/giây).
     * @param initialAngle  Góc ban đầu của cả nhóm (độ).
     */
    public RotatingBrickGroup(List<Brick> bricks, double centerX, double centerY,
                              double radius, double rotationSpeed, double initialAngle) {
        this.bricksInGroup = new ArrayList<>(bricks); // Tạo bản sao để tránh sửa đổi danh sách gốc
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.rotationSpeed = rotationSpeed;
        this.currentGroupAngle = initialAngle; // Góc ban đầu của cả nhóm

        // Thiết lập vị trí ban đầu cho từng viên gạch trong nhóm dựa trên góc ban đầu của nhóm
        // và góc tương đối của từng viên gạch (sẽ được tính trong LevelBuilder)
        // Tuy nhiên, để đơn giản, chúng ta sẽ giả sử LevelBuilder đã tính toán vị trí ban đầu.
        // Logic update sẽ điều chỉnh vị trí này.
        updateBrickPositions(); // Cập nhật vị trí ngay khi khởi tạo
    }

    /**
     * Cập nhật vị trí của tất cả các viên gạch trong nhóm.
     *
     * @param dt Thời gian trôi qua kể từ khung hình trước.
     */
    public void update(double dt) {
        // 1. Cập nhật góc xoay của toàn bộ nhóm
        this.currentGroupAngle += rotationSpeed * dt;

        // Giữ góc trong khoảng [0, 360) (tùy chọn)
        if (this.currentGroupAngle > 360) {
            this.currentGroupAngle -= 360;
        } else if (this.currentGroupAngle < 0) {
            this.currentGroupAngle += 360;
        }

        // 2. Cập nhật vị trí của từng viên gạch trong nhóm
        updateBrickPositions();
    }

    /**
     * Tính toán và thiết lập vị trí mới cho từng viên gạch dựa trên góc xoay hiện tại.
     * Đây là hàm nội bộ được gọi bởi update() và constructor.
     */
    private void updateBrickPositions() {
        // Góc giữa mỗi viên gạch trong nhóm (nếu chúng được tạo cách đều nhau)
        double angleStep = 360.0 / bricksInGroup.size();

        for (int i = 0; i < bricksInGroup.size(); i++) {
            Brick brick = bricksInGroup.get(i);
            if (brick.isDestroyed()) {
                continue; // Bỏ qua gạch đã bị phá hủy
            }

            // Tính toán góc riêng của viên gạch này trong nhóm
            // Bắt đầu từ currentGroupAngle và thêm góc bước cho từng viên
            double brickRelativeAngle = currentGroupAngle + (i * angleStep);

            // Chuyển đổi góc từ Độ sang Radian
            double angleRad = toRadians(brickRelativeAngle);

            // Tính toán vị trí X và Y mới (Tọa độ Đề-các)
            // Lưu ý: Căn giữa viên gạch vào vị trí tính toán
            double newX = centerX + (radius * cos(angleRad)) - (brick.getWidth() / 2.0);
            double newY = centerY + (radius * sin(angleRad)) - (brick.getHeight() / 2.0);

            // Áp dụng vị trí mới cho viên gạch
            brick.setX(newX);
            brick.setY(newY);

            // Nếu Brick có logic cập nhật riêng (như hiệu ứng, v.v.), hãy gọi:
            // brick.update(dt);
        }
    }

    /**
     * Trả về danh sách các viên gạch trong nhóm này.
     * Cần thiết để GameManager có thể render và kiểm tra va chạm.
     */
    public List<Brick> getBricksInGroup() {
        return bricksInGroup;
    }
}