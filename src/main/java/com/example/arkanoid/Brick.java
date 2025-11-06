package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Brick extends GameObject {

    private enum BrickType {
        STONE,      // Loại 3
        CLOUD,      // Loại 2
        INDESTRUCTIBLE, // Loại 4
        SPECIAL_STONE // <-- THÊM MỚI: Loại 5
    }

    private int hits;
    private boolean destroyed = false;
    private BrickType type;


    // --- THÊM MẢNG ẢNH MỚI ---
    private static final Image[] stoneImages = new Image[3];
    private static final Image[] cloudImages = new Image[2];
    private static Image indestructibleImage;
    // Mảng cho gạch 5 hit (Loại 5)
    private static final Image[] specialStoneImages = new Image[5];
    // --- KẾT THÚC THÊM ---

    static {
        try {
            // Tải ảnh cho gạch ĐÁ (Loại 3)
            stoneImages[2] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_3.png"));
            stoneImages[1] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_3.1.png"));
            stoneImages[0] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_3.2.png"));

            // Tải ảnh cho gạch MÂY (Loại 2)
            cloudImages[1] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_2.png"));
            cloudImages[0] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_2.1.png"));

            // Tải ảnh cho gạch KHÔNG THỂ PHÁ HỦY (Loại 4)
            indestructibleImage = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brickNotdestroy.png"));

            // --- TẢI ẢNH MỚI CHO LOẠI 5 ---
            specialStoneImages[4] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_5.png")); // 5 hits
            specialStoneImages[3] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_5.1.png")); // 4 hits
            specialStoneImages[2] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_5.2.png")); // 3 hits
            specialStoneImages[1] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_5.3.png")); // 2 hits
            specialStoneImages[0] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_5.4.png")); // 1 hit
            // --- KẾT THÚC TẢI ---

            // (Kiểm tra lỗi)
            if (stoneImages[0] == null || stoneImages[1] == null || stoneImages[2] == null) {
                System.err.println("Lỗi nghiêm trọng: Thiếu ảnh trạng thái cho gạch ĐÁ (Loại 3).");
            }
            if (cloudImages[0] == null || cloudImages[1] == null) {
                System.err.println("Lỗi nghiêm trọng: Thiếu ảnh trạng thái cho gạch MÂY (Loại 2).");
            }
            if (indestructibleImage == null) {
                System.err.println("Lỗi nghiêm trọng: Không thể tải ảnh cho gạch không thể phá hủy (Loại 4).");
            }
            // (Kiểm tra lỗi cho ảnh mới)
            if (specialStoneImages[0] == null || specialStoneImages[1] == null || specialStoneImages[2] == null || specialStoneImages[3] == null || specialStoneImages[4] == null) {
                System.err.println("Lỗi nghiêm trọng: Thiếu ảnh trạng thái cho gạch ĐẶC BIỆT (Loại 5).");
            }

        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng: Không thể tải được ảnh cho Brick.");
            e.printStackTrace();
        }
    }

    // --- SỬA LẠI CONSTRUCTOR ---
    public Brick(double x, double y, double w, double h, int typeCode) {
        super(x, y, w, h);

        switch (typeCode) {
            case 5: // <-- THÊM CASE 5
                this.type = BrickType.SPECIAL_STONE;
                this.hits = 5;
                break;
            case 4:
                this.type = BrickType.INDESTRUCTIBLE;
                this.hits = 1;
                break;
            case 3:
                this.type = BrickType.STONE;
                this.hits = 3;
                break;
            case 2:
                this.type = BrickType.CLOUD;
                this.hits = 2;
                break;
            default:
                // (Gạch loại 1 mặc định là gạch đá 1 hit)
                this.type = BrickType.STONE;
                this.hits = 1;
                if (stoneImages[0] == null) { // Đảm bảo ảnh tồn tại
                    stoneImages[0] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_3.2.png"));
                }
                break;
        }
    }
    // --- KẾT THÚC SỬA CONSTRUCTOR ---

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isIndestructible() {
        return this.type == BrickType.INDESTRUCTIBLE;
    }

    public int getHits() {
        return this.hits;
    }

    // (Hàm takeHit không đổi)
    public boolean takeHit() {
        if (this.type == BrickType.INDESTRUCTIBLE) {
            return false;
        }
        hits--;
        if (hits <= 0) {
            destroyed = true;
        }
        return destroyed;
    }

    @Override
    public void update(double dt) { /* Gạch tĩnh không cần update */ }

    // --- SỬA HÀM RENDER ---
    @Override
    public void render(GraphicsContext gc) {
        if (destroyed) {
            return;
        }

        Image imageToDraw = null;

        switch (this.type) {
            case INDESTRUCTIBLE:
                imageToDraw = indestructibleImage;
                break;

            case STONE: // Gạch ĐÁ (Loại 3)
                if (hits >= 3) {
                    imageToDraw = stoneImages[2];
                } else if (hits == 2) {
                    imageToDraw = stoneImages[1];
                } else if (hits == 1) {
                    imageToDraw = stoneImages[0];
                }
                break;

            case CLOUD: // Gạch MÂY (Loại 2)
                if (hits >= 2) {
                    imageToDraw = cloudImages[1];
                } else if (hits == 1) {
                    imageToDraw = cloudImages[0];
                }
                break;

            // --- THÊM CASE MỚI CHO LOẠI 5 ---
            case SPECIAL_STONE:
                if (hits >= 5) {
                    imageToDraw = specialStoneImages[4]; // 5.png
                } else if (hits == 4) {
                    imageToDraw = specialStoneImages[3]; // 5.1.png
                } else if (hits == 3) {
                    imageToDraw = specialStoneImages[2]; // 5.2.png
                } else if (hits == 2) {
                    imageToDraw = specialStoneImages[1]; // 5.3.png
                } else if (hits == 1) {
                    imageToDraw = specialStoneImages[0]; // 5.4.png
                }
                break;
            // --- KẾT THÚC THÊM ---
        }

        // (Code vẽ dự phòng không đổi)
        if (imageToDraw != null && !imageToDraw.isError()) {
            gc.drawImage(imageToDraw, x, y, width, height);
        } else {
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(x, y, width, height);
            if (imageToDraw == null) {
                System.err.println("Lỗi render: imageToDraw là null cho type " + this.type + " với " + this.hits + " hits.");
            }
        }
    }
}