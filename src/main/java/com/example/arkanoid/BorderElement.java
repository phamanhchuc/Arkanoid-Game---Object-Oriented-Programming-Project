package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Lớp này đại diện cho một đối tượng trang trí tĩnh (như dây leo).
 * Nó chỉ có chức năng hiển thị và không thể bị phá hủy hay tương tác.
 */
public class BorderElement extends GameObject {

    // Tải ảnh dây leo một lần duy nhất cho tất cả các đối tượng
    private static final Image borderImage;
    public static double BORDER_WIDTH = 0;
    public static double BORDER_HEIGHT = 0;

    static {
        Image img = null;
        try {
            img = new Image(BorderElement.class.getResourceAsStream("/com/example/arkanoid/images/ivy.png"));
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh viền ivy.jpg");
            e.printStackTrace();
        }
        borderImage = img;
        if (borderImage != null) {
            BORDER_WIDTH = borderImage.getWidth();
            BORDER_HEIGHT = borderImage.getHeight();
        }
    }

    public BorderElement(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    @Override
    public void update(double dt) {
        // Không làm gì cả, đây là đối tượng tĩnh
    }

    @Override
    public void render(GraphicsContext gc) {
        if (borderImage != null) {
            gc.drawImage(borderImage, x, y, width, height);
        }
    }
}
