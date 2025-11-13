# Arkanoid Game - Object-Oriented Programming Project
---
## Author
Group: Ăn Cơm Với Rau Má - Class: INT2204 4
1. Lường Tú Quân - 24020283
2. Trần Bảo Long - 24020211
3. Phạm Ánh Chúc - 24020049
4. Trịnh Tiến Kiệt - 24020193
Instructor: Kiều Văn Tuyên, Vũ Đức Hiếu <br>
Semester: HK1 - 2025_2026
---
## Description
This is a classic Arkanoid (brick-breaker) game developed in Java, serving as a project demonstrating the application of Object-Oriented Programming (OOP) principles and design patterns.

### Key Features

** Technology **
- Developed using **Java 17+** with **JavaFX** for the Graphical User Interface (GUI) and game loop.

** OOP Principles **
- **Inheritance:** Base classes like `GameObject` and `MovableObject` are extended by entities such as `Ball`, `Paddle`, and `Brick`.
- **Polymorphism & Abstraction:** Implemented through abstract classes and interfaces (e.g., `CollisionStrategy`).
- **Encapsulation:** Classes like `GameManager` and `SoundManager` hide internal logic and provide public methods for interaction.

** Design Patterns **
- **Factory Method (Abstract Factory):** `BrickFactory` with concrete implementations like `NormalBrickFactory` and `IndestructibleBrickFactory` to create different brick types.
- **Strategy:** `CollisionStrategy` (e.g., `NormalCollisionStrategy`) defines collision handling behaviors for the ball.
- **Simple Factory:** `PowerUpFactory` randomly creates various power-up items.

** System Features **
- Sound effects (SFX) and background music for each level, managed by `SoundManager`.
- Save/load system for the leaderboard using `HighScores` and `ScoreEntry` (implements `Serializable`).
- Smooth, responsive game loop using JavaFX's `AnimationTimer` in `GameManager`.

---

### Game Mechanics

- **Core Gameplay:** Control a `Paddle` to bounce a `Ball` and destroy `Bricks`.
- **Power-ups:** Collect items dropped from broken bricks to gain special abilities (e.g., `CROSS_BOW`, `MULTI_BALL`, `PIERCING_SHOT`).
- **Diverse Levels:** `LevelBuilder` reads configuration files to construct levels.
- **Dynamic Elements:** Includes moving bricks (`MovingB17+     | Core language: Used to write all game logic, build classes (Ball, Paddle, Brick), manage game logic (GameManager.java) and states, define data structures (List<Brick>, Map<Integer, BrickFactory>). |
| JavaFX     | Integrated with Java SE | GUI framework & game rendering: Provides Application, Stage, Scene for UI; FXMLLoader for FXML layouts; Canvas & GraphicsContext for rendering game objects; AnimationTimer for game loop; FadeTransition & ScaleTransition for UI effects; MediaPlayer & AudioClip for background music and SFX; handles keyboard and mouse events. |
# License
We use this project for learning and reference purposes.  
All submitted work complies with our institution’s academic integrity policies.  
Any code or ideas referenced from external sources have been adapted, understood, and are our responsibility.
---
# Note
-The game was developed as part of the Object-Oriented Programming with Java course curriculum.
-All code is written by group members with guidance from the instructor.
-Some assets (images, sounds) may be used for educational purposes under fair use.
-The project demonstrates practical application of OOP concepts and design patterns.
