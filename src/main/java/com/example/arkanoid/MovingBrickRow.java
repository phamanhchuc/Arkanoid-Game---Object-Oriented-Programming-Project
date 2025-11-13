package com.example.arkanoid;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;

// MovingBrickRow sẽ quản lý một nhóm các viên gạch
public class MovingBrickRow extends GameObject { // Kế thừa GameObject vì nó có vị trí và kích thước tổng thể

    private List<Brick> bricksInRow; // Danh sách các viên gạch trong hàng
    private double currentDx;        // Vận tốc X hiện tại của cả hàng
    private double currentDy;        // Vận tốc Y hiện tại của cả hàng (nếu có di chuyển dọc)

    private double limitLeft;        // Giới hạn di chuyển bên trái của hàng
    private double limitRight;       // Giới hạn di chuyển bên phải của hàng
    private double limitTop;         // Giới hạn di chuyển phía trên của hàng
    private double limitBottom;      // Giới hạn di chuyển phía dưới của hàng

    // LƯU Ý: x, y, width, height của MovingBrickRow sẽ đại diện cho bounding box của cả hàng

    public MovingBrickRow(double x, double y, double width, double height,
                          double initialDx, double initialDy,
                          double limitLeft, double limitRight, double limitTop, double limitBottom) {
        super(x, y, width, height); // x,y,w,h ban đầu sẽ là vị trí và kích thước tạm thời/tổng của hàng
        this.bricksInRow = new ArrayList<>();
        this.currentDx = initialDx;
        this.currentDy = initialDy;
        this.limitLeft = limitLeft;
        this.limitRight = limitRight;
        this.limitTop = limitTop;
        this.limitBottom = limitBottom;
    }

    // Thêm một viên gạch vào hàng
    public void addBrick(Brick brick) {
        this.bricksInRow.add(brick);
        // Cập nhật kích thước tổng thể của hàng (nếu cần)
        // Đây là một cách đơn giản, bạn có thể cần tính toán kỹ hơn
        if (bricksInRow.size() == 1) {
            this.x = brick.getX();
            this.y = brick.getY();
            this.width = brick.getWidth();
            this.height = brick.getHeight();
        } else {
            // Cập nhật bounding box của hàng
            this.x = Math.min(this.x, brick.getX());
            this.y = Math.min(this.y, brick.getY());
            this.width = Math.max(this.x + this.width, brick.getX() + brick.getWidth()) - this.x;
            this.height = Math.max(this.y + this.height, brick.getY() + brick.getHeight()) - this.y;
        }
    }

    public List<Brick> getBricksInRow() {
        return bricksInRow;
    }

    @Override
    public void update(double dt) {
        // --- Cập nhật vị trí của TẤT CẢ các viên gạch trong hàng ---
        for (Brick brick : bricksInRow) {
            // Cập nhật vị trí x, y của từng viên gạch
            // Sử dụng currentDx, currentDy của MovingBrickRow
            brick.setX(brick.getX() + currentDx * dt);
            brick.setY(brick.getY() + currentDy * dt);
        }

        // --- Cập nhật bounding box của MovingBrickRow ---
        // (Điều này quan trọng để kiểm tra va chạm của cả hàng với giới hạn)
        if (!bricksInRow.isEmpty()) {
            double minX = bricksInRow.get(0).getX();
            double maxX = bricksInRow.get(0).getX() + bricksInRow.get(0).getWidth();
            double minY = bricksInRow.get(0).getY();
            double maxY = bricksInRow.get(0).getY() + bricksInRow.get(0).getHeight();

            for (Brick brick : bricksInRow) {
                minX = Math.min(minX, brick.getX());
                maxX = Math.max(maxX, brick.getX() + brick.getWidth());
                minY = Math.min(minY, brick.getY());
                maxY = Math.max(maxY, brick.getY() + brick.getHeight());
            }
            this.x = minX;
            this.y = minY;
            this.width = maxX - minX;
            this.height = maxY - minY;
        }

        // --- Logic đảo chiều cho CẢ HÀNG khi chạm giới hạn ---
        // Di chuyển ngang (X-axis)
        if (currentDx > 0 && (this.x + this.width) >= limitRight) {
            currentDx *= -1; // Đảo chiều vận tốc cho cả hàng
            // Đảm bảo cả hàng không bị kẹt ở biên
            double overshoot = (this.x + this.width) - limitRight;
            for (Brick brick : bricksInRow) {
                brick.setX(brick.getX() - overshoot);
            }
        } else if (currentDx < 0 && this.x <= limitLeft) {
            currentDx *= -1; // Đảo chiều vận tốc cho cả hàng
            // Đảm bảo cả hàng không bị kẹt ở biên
            double overshoot = limitLeft - this.x;
            for (Brick brick : bricksInRow) {
                brick.setX(brick.getX() + overshoot);
            }
        }

        // Di chuyển dọc (Y-axis) - nếu cần
        if (currentDy > 0 && (this.y + this.height) >= limitBottom) {
            currentDy *= -1;
            double overshoot = (this.y + this.height) - limitBottom;
            for (Brick brick : bricksInRow) {
                brick.setY(brick.getY() - overshoot);
            }
        } else if (currentDy < 0 && this.y <= limitTop) {
            currentDy *= -1;
            double overshoot = limitTop - this.y;
            for (Brick brick : bricksInRow) {
                brick.setY(brick.getY() + overshoot);
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        // Mỗi viên gạch tự render, MovingBrickRow không render trực tiếp
        // (Hoặc bạn có thể vẽ bounding box debug ở đây)
    }
}