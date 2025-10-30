package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

// --- THÊM CÁC IMPORT NÀY ---
import java.util.ArrayList;
import java.util.List;
// --- KẾT THÚC THÊM ---

public class Projectile extends MovableObject {
    private double speed = 900;
    private boolean destroyed = false;
    private boolean isPiercing = false;

    // --- THÊM MỚI: Danh sách gạch đã va chạm ---
    private List<Brick> bricksHit = new ArrayList<>();
    // --- KẾT THÚC THÊM MỚI ---

    private static Image arrowImage;
    private static Image meomeoBulletImage;

    // (Khối static debug tải ảnh không đổi - Giữ nguyên khối static debug của bạn)
    static {
        try {
            String arrowPath = "/com/example/arkanoid/images/arrow.png";
            String meomeoPath = "/com/example/arkanoid/images/dagger.png";

            java.io.InputStream arrowStream = Projectile.class.getResourceAsStream(arrowPath);
            if (arrowStream == null) {
                System.err.println("LỖI DEBUG: Không tìm thấy file arrow tại: " + arrowPath);
            } else {
                arrowImage = new Image(arrowStream);
                if (arrowImage.isError()) System.err.println("LỖI DEBUG: File arrow BỊ LỖI KHI ĐỌC: " + arrowPath);
                else System.out.println("DEBUG: Tải arrow_projectile.png thành công.");
                arrowStream.close();
            }

            java.io.InputStream meomeoStream = Projectile.class.getResourceAsStream(meomeoPath);
            if (meomeoStream == null) {
                System.err.println("LỖI DEBUG: Không tìm thấy file meomeo tại: " + meomeoPath);
                System.err.println(">>> HÃY KIỂM TRA LẠI: file có nằm trong 'src/main/resources" + meomeoPath + "' không?");
            } else {
                meomeoBulletImage = new Image(meomeoStream);
                if (meomeoBulletImage.isError()) {
                    System.err.println("LỖI DEBUG: File meomeo BỊ LỖI KHI ĐỌC (file bị hỏng?): " + meomeoPath);
                    if(meomeoBulletImage.getException() != null) meomeoBulletImage.getException().printStackTrace();
                } else {
                    System.out.println("DEBUG: Tải meomeobullet.png thành công.");
                }
                meomeoStream.close();
            }
        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng trong khối static của Projectile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // (Constructor đã sửa lỗi căn giữa)
    public Projectile(double x, double y, double w, double h, boolean isPiercing) {
        super(x, y, w, h);

        this.isPiercing = isPiercing;

        if (isPiercing) {
            this.dy = -300;
            this.image = meomeoBulletImage;
            this.width = 50;  // Bạn có thể chỉnh kích thước ở đây
            this.height = 100; // Bạn có thể chỉnh kích thước ở đây
            this.x = x - (this.width / 2);
            this.y = y - this.height;
        } else {
            this.dy = -speed;
            this.image = arrowImage;
            this.width = w;
            this.height = h;
            this.x = x - (w / 2);
            this.y = y - h;
        }
    }

    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean d) { this.destroyed = d; }
    public boolean isPiercing() { return this.isPiercing; }

    // --- THÊM 2 HÀM MỚI ĐỂ SỬA LỖI ---
    /**
     * Kiểm tra xem đạn này đã từng va chạm gạch này chưa.
     */
    public boolean hasHitBrick(Brick b) {
        return bricksHit.contains(b);
    }

    /**
     * Đánh dấu gạch này là đã va chạm.
     */
    public void addHitBrick(Brick b) {
        bricksHit.add(b);
    }
    // --- KẾT THÚC THÊM MỚI ---

    @Override
    public void update(double dt) {
        move(dt);
    }

    @Override
    public void render(GraphicsContext gc) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, x, y, width, height);
        } else {
            // Vẽ dự phòng
            gc.setFill(isPiercing ? Color.PURPLE : Color.YELLOW);
            gc.fillRect(x, y, width, height);
        }
    }
}