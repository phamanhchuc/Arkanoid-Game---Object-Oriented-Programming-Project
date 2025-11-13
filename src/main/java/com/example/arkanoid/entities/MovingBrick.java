package com.example.arkanoid.entities;

public class MovingBrick extends Brick {

    // Phạm vi di chuyển
    private double startX; // Giới hạn di chuyển bên trái
    private double endX;   // Giới hạn di chuyển bên phải
    private double startY; // Giới hạn di chuyển phía trên
    private double endY;   // Giới hạn di chuyển phía dưới

    /**
     * Constructor cho MovingBrick.
     *
     * @param x         Vị trí X ban đầu.
     * @param y         Vị trí Y ban đầu.
     * @param w         Chiều rộng.
     * @param h         Chiều cao.
     * @param typeCode  Mã loại gạch.
     * @param initialDx Vận tốc X ban đầu (ví dụ: 50.0).
     * @param initialDy Vận tốc Y ban đầu (ví dụ: 0).
     * @param startX    Giới hạn X tối thiểu.
     * @param endX      Giới hạn X tối đa.
     * @param startY    Giới hạn Y tối thiểu.
     * @param endY      Giới hạn Y tối đa.
     */
    public MovingBrick(double x, double y, double w, double h, int typeCode,
                       double initialDx, double initialDy,
                       double startX, double endX, double startY, double endY) {

        // Gọi constructor của lớp cha (Brick), lúc này Brick kế thừa MovableObject
        super(x, y, w, h, typeCode);

        // Gán vận tốc di chuyển ban đầu (thay thế vận tốc 0 mặc định của Brick)
        this.setDx(initialDx);
        this.setDy(initialDy);

        // Gán phạm vi di chuyển
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
    }

    @Override
    public void update(double dt) {
        if (isDestroyed() || isIndestructible()) {
            return; // Không di chuyển nếu đã bị phá hủy hoặc là gạch bất tử
        }

        // 1. Cập nhật vị trí
        super.move(dt); // Sử dụng phương thức move() từ MovableObject

        // 2. Logic đảo chiều (Bounce)

        // Di chuyển ngang (X-axis)
        if (this.getDx() > 0 && this.x + this.width >= endX) {
            // Đang đi sang phải và chạm giới hạn phải
            this.setDx(this.getDx() * -1);
            // Đảm bảo đối tượng không bị kẹt ở biên
            this.x = endX - this.width;
        } else if (this.getDx() < 0 && this.x <= startX) {
            // Đang đi sang trái và chạm giới hạn trái
            this.setDx(this.getDx() * -1);
            // Đảm bảo đối tượng không bị kẹt ở biên
            this.x = startX;
        }

        // Di chuyển dọc (Y-axis)
        if (this.getDy() > 0 && this.y + this.height >= endY) {
            // Đang đi xuống và chạm giới hạn dưới
            this.setDy(this.getDy() * -1);
            this.y = endY - this.height;
        } else if (this.getDy() < 0 && this.y <= startY) {
            // Đang đi lên và chạm giới hạn trên
            this.setDy(this.getDy() * -1);
            this.y = startY;
        }
    }

    // Phương thức render() sẽ được kế thừa từ Brick, tự động vẽ ở vị trí x, y mới.
}