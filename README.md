<<<<<<< HEAD
# Arkanoid Game - Object-Oriented Programming Project
---
## Author
Group: Ăn Cơm Với Rau Má - Class: INT2204 4
1. Lường Tú Quân - 24020283
2. Trần Bảo Long - 24020211
3. Phạm Ánh Chúc - 24020049
4. Trịnh Tiến Kiệt - 24020193 <br>
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
- **Dynamic Elements:** Includes moving bricks (`MovingBrick`, `MovingBrickRow`) and complex formations (`RotatingBrickGroup`).
- **Boss Battles:** Confront end-of-level bosses (`Boss`, `BossLevel2`, `BossLevel3`) with unique attack patterns and specific weaknesses (e.g., `HeartBrick`).

---
## UML Diagram
### Class Diagram
[Diagram](https://drive.google.com/file/d/1barFJ6GdpOsDc6i11W3BBaqWHZzBSYMi/view?usp=sharing)
---
## Design Patterns Implementation
### 1. Abstract Factory (and Factory)
This pattern is clearly used to create different types of **Bricks**.

**Purpose:** Allows creating "families" of objects (brick types) without specifying their concrete classes at the usage site.

**Implementation:**
- `BrickFactory.java`: Abstract Factory defining the `createBrick` method.
- `NormalBrickFactory.java`: Concrete Factory that creates normal bricks.
- `IndestructibleBrickFactory.java`: Concrete Factory that creates indestructible bricks.
- `LevelBuilder.java`: Client class using these factories (via a `Map<Integer, BrickFactory>`) to build levels. It calls `factory.createBrick(...)` without knowing the exact concrete factory.
- `PowerUpFactory.java`: Implements a **Simple Factory** to create different power-ups randomly via `createRandomPowerUp`.

---

### 2. Strategy Pattern
Used for handling **ball collision logic**.

**Purpose:** Define a set of algorithms (strategies) and make them interchangeable.

**Implementation:**
- `CollisionStrategy.java`: Interface (Strategy) defining the method `handleCollision`.
- `NormalCollisionStrategy.java`: Concrete Strategy implementing normal collision behavior.
- `Ball.java`: Context class holding a reference to `CollisionStrategy`. When a ball collides, it delegates the handling to the current strategy. (Additional strategies like `FireCollisionStrategy` or `IceCollisionStrategy` can be added.)

---

### 3. State Pattern
Used (in a simple form) to manage different **ball states**.

**Purpose:** Allow an object to change its behavior when its internal state changes.

**Implementation:**
- `Ball.java`: Defines an enum `BallState { NORMAL, FIRE, ICE }` and stores the current state (`private BallState currentState`).
- Ball behavior changes based on state. For example, `NormalCollisionStrategy.java` checks `ball.getState() == Ball.BallState.FIRE` to determine damage to bosses. `Ball.java` also has a `stateTimer` to revert to `NORMAL` after a duration.

---

### 4. Singleton (and Static Utility Class)
Ensures only **one instance** of a class exists and provides a global access point.

**Purpose:** Manage shared resources or services, like audio or game management.
---
## Multithreading Implementation

### 1. Implicit Multithreading
The project uses **multithreading** implicitly, even if you don't explicitly create threads:

- **SoundManager.java**:
  - When calling `MediaPlayer.play()` to play background music, JavaFX automatically runs a **background thread** to load (stream) and decode the audio file.
  - Similarly, calling `AudioClip.play()` for sound effects (SFX) is managed on a separate audio thread by JavaFX.
  
**Why:** Running audio on the main UI thread would freeze the game briefly whenever loading or playing audio. JavaFX handles this automatically to keep the game smooth.

---

### 2. The JavaFX Concurrency Model
JavaFX handles multiple "simultaneous" tasks on a **single FX Application Thread (FXAT)** rather than creating multiple threads:

- **AnimationTimer (MainController.java)**:
  - This is the main game loop.
  - AnimationTimer is a **callback**, not a new thread. JavaFX calls the `handle(long now)` method once per frame (usually 60 times/sec) just before rendering.
  - All `gameManager.update(elapsedSeconds)` and `gameManager.render(gc)` run on FXAT. This ensures game logic and rendering are synchronized, avoiding issues like `IllegalStateException: Not on FX Application Thread`.

- **Timeline, PauseTransition, FadeTransition, ScaleTransition (MenuController, LoginController)**:
  - All UI animations (e.g., button fade, blink, scale) are **scheduled tasks**, not new threads.
  - They instruct FXAT: "after X milliseconds, update this property a little."
  - This allows multiple effects to run **simultaneously** (e.g., a button fading and scaling) without manual thread management.
---
## Installation
1. Clone the project from the repository.
2. Open the project in the IDE.
3. Run the project.

## Usage

### Controls

| Key        | Action                    |
|------------|---------------------------|
| ←          | Move paddle left          |
| →          | Move paddle right         |
| **SPACE**  | Launch ball               |
| **P**      | Pause game                |
| **Q**      | Quit to menu              |

### How to play
- **Start the game:** Click **Start button** from the main menu.  
- **Control the paddle:** Use **← / →** to move left and right.  
- **Launch the ball:** Press **SPACE** to release the ball from the paddle.  
- **Destroy bricks:** Bounce the ball to break bricks across the level.  
- **Collect power-ups:** Catch falling items to gain special abilities.  
- **Avoid bad items:** Stay away from harmful power-downs that reduce your abilities.  
- **Face enemies:** Encounter various enemies in each level and react to their attacks or projectiles.  
- **Avoid losing the ball:** Prevent the ball from falling below the paddle.  
- **Complete the level:** Destroy all destructible bricks to progress.

### Power-Ups

| Power-Up        | Description                                                      |
|-----------------|------------------------------------------------------------------|
| 🔥 **Fire Ball** | A harmful item — instantly **removes 1 life** when collected.    |
| ➕ **The Cross**  | Grants **+1 extra life**.                                        |
| ⭐ **Star**       | Allows the paddle to **shoot a piercing dagger**.                |
| ⚙️ **Gear**       | Upgrades the paddle into a **Crossbow Paddle** that can shoot.   |
| 🎲 **Dice**       | A random ball fires **3 upward projectiles** from itself.        |
| 👾 **Enemy Drops** | Some enemies drop **special items** with various effects.        |

### Scoring System
CLOUD Brick (type 2, 2 hits): scoreValue = 200

STONE Brick (type 3, 3 hits): scoreValue = 300

SPECIAL_STONE Brick (type 5, 5 hits): scoreValue = 500
---
# Demo
## Screenshot
### 🕹️ Level 1
![Level 1 Screenshot](https://drive.google.com/uc?export=view&id=1UyQpwTpmSG1nT5p4Pb20NyFU1pdogLWD)

### ⚔️ Level 2
![Level 2 Screenshot](https://drive.google.com/uc?export=view&id=1e513ik5r0rJAAL6I3EqdNfkbTzLE1Kwm)

### 👑 Level 3 (Boss Stage)
![Level 3 Screenshot](https://drive.google.com/uc?export=view&id=1idmQlYGVPGZvLhAA3vvd4drvN_e2VRP8)
## Video Demo
[▶️ Watch Demo on Google Drive](https://drive.google.com/file/d/1nNvsTcCXvubSUeF2CnsUwNaNqhzU2f-z/view?usp=drive_link)

---
# Future Improvements
...
---
# Technologies Used
| Technology | Version | Purpose |
|------------|---------|---------|
| Java       | 17+     | Core language: Used to write all game logic, build classes (Ball, Paddle, Brick), manage game logic (GameManager.java) and states, define data structures (List<Brick>, Map<Integer, BrickFactory>). |
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
=======
# Arkanoid-Game---Object-Oriented-Programming-Project
>>>>>>> fa48995cacb6f115ed3b6ac462db1b4ea1fdee0d
