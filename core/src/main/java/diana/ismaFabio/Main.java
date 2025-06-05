package diana.ismaFabio;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.ArrayList;
import java.util.Iterator;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private FitViewport viewport;
    private BitmapFont font;

    private Texture soldierTexture, enemyTexture, bulletTexture, muroTexture;
    private Sprite soldierSprite,powerupSprite;

    private ArrayList<Sprite> enemies = new ArrayList<>();
    private ArrayList<Sprite> bullets = new ArrayList<>();

    private final int TILE_SIZE = 64;
    private int score = 0;
    private int health = 3;
    private boolean gameOver = false;

    private int[][] maze = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,0,1,0,1,1,0,0,0,0,0,1,0,1},
        {1,0,1,0,0,0,0,1,0,0,0,0,0,1,0,1},
        {1,0,1,1,1,1,0,1,0,1,0,0,0,1,0,1},
        {1,0,0,0,0,1,0,0,0,1,0,0,1,1,0,1},
        {1,1,1,1,0,1,1,1,0,1,0,0,0,0,0,1},
        {1,0,0,1,0,0,0,1,0,1,0,1,1,1,0,1},
        {1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1},
    };


    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new FitViewport(640, 480);
        font = new BitmapFont();

        soldierTexture = new Texture("pato.png");
        enemyTexture = new Texture("enemy.png");
        bulletTexture = new Texture("pato.png");
        muroTexture = new Texture("muro.png");
        Texture powerupTexture = new Texture("vida.png");

        soldierSprite = new Sprite(soldierTexture);
        soldierSprite.setPosition(80, 240);
        soldierSprite.setSize(TILE_SIZE, TILE_SIZE);

        for (int i = 0; i < 15; i++) {
            Sprite enemy = new Sprite(enemyTexture);
            enemy.setPosition(MathUtils.random(400, 600), MathUtils.random(100, 380));
            enemy.setSize(TILE_SIZE, TILE_SIZE);
            enemies.add(enemy);
        }
    }

    @Override
    public void render() {

        if (!gameOver) {
            input();
            updateBullets();
            moveEnemies();
            checkCollisions();
        }
        draw();
    }

    private void input() {
        float speed = 150f;
        float delta = Gdx.graphics.getDeltaTime();
        float oldX = soldierSprite.getX();
        float oldY = soldierSprite.getY();
        float newX = oldX;
        float newY = oldY;

        if (Gdx.input.isKeyPressed(Keys.RIGHT)) newX += speed * delta;
        if (Gdx.input.isKeyPressed(Keys.LEFT)) newX -= speed * delta;
        if (Gdx.input.isKeyPressed(Keys.UP)) newY += speed * delta;
        if (Gdx.input.isKeyPressed(Keys.DOWN)) newY -= speed * delta;

        // **Verificación de colisión con muros**
        if (!isCollidingWithWall(newX, newY)) {
            soldierSprite.setPosition(newX, newY);
        } else {
            soldierSprite.setPosition(oldX, oldY);
        }

        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            Sprite bullet = new Sprite(bulletTexture);
            bullet.setPosition(soldierSprite.getX() + TILE_SIZE, soldierSprite.getY() + TILE_SIZE / 2);
            bullet.setSize(16, 16);
            bullets.add(bullet);
        }
    }

    private boolean isCollidingWithWall(float x, float y) {
        int col = (int) (x / TILE_SIZE);
        int row = (int) (y / TILE_SIZE);

        if (row < 0 || row >= maze.length || col < 0 || col >= maze[0].length) {
            return true; // Fuera del laberinto cuenta como muro
        }

        return maze[row][col] == 1; // Si la celda es 1, hay un muro
    }

    private void updateBullets() {
        Iterator<Sprite> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Sprite bullet = bulletIterator.next();
            bullet.setX(bullet.getX() + 200 * Gdx.graphics.getDeltaTime());
            if (bullet.getX() > 640) bulletIterator.remove();  // Eliminar balas fuera de pantalla
        }
    }

    private void moveEnemies() {
        for (Sprite enemy : enemies) {
            enemy.setX(enemy.getX() + MathUtils.random(-1, 1) * 50 * Gdx.graphics.getDeltaTime());
            enemy.setY(enemy.getY() + MathUtils.random(-1, 1) * 50 * Gdx.graphics.getDeltaTime());
        }
    }

    private void checkCollisions() {
        Iterator<Sprite> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Sprite bullet = bulletIterator.next();
            Iterator<Sprite> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Sprite enemy = enemyIterator.next();
                if (bullet.getBoundingRectangle().overlaps(enemy.getBoundingRectangle())) {
                    enemyIterator.remove();
                    bulletIterator.remove();
                    score++;
                    break;
                }
            }
        }
        for (Sprite enemy : enemies) {
            if (soldierSprite.getBoundingRectangle().overlaps(enemy.getBoundingRectangle())) {
                health--; // 🔥 Reduce vida
                enemies.remove(enemy); // 🔥 Elimina al enemigo tras el choque
                break;
            }
        }
        if (health <= 0) {
            gameOver = true; // 🔥 Si vida es 0, "GAME OVER"
        }
        if (score >= 5) gameOver = true;

        // 🔹 Si todos los enemigos son eliminados, el jugador gana
        if (enemies.isEmpty()) {
            gameOver = true; // 🔥 Activa la pantalla de victoria
        }

        // 🔹 Si se queda sin vida, muestra "Game Over"
        if (health <= 0) {
            gameOver = true;
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.GRAY);
        batch.begin();

        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[row].length; col++) {
                if (maze[row][col] == 1) {
                    batch.draw(muroTexture, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        soldierSprite.draw(batch);
        for (Sprite enemy : enemies) enemy.draw(batch);
        for (Sprite bullet : bullets) bullet.draw(batch);

        // 🔥 Mostrar las vidas
        font.getData().setScale(2);
        font.setColor(Color.BLACK);
        font.draw(batch, "VIDA: " + health, 10, 470);

        // 🔹 Muestra el mensaje correspondiente
        if (gameOver && enemies.isEmpty()) {
            font.setColor(Color.GREEN);
            font.draw(batch, "¡HAS GANADO!", 250, 240);
        } else if (gameOver) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER", 250, 240);
        }

        batch.end();
    }


    @Override
    public void dispose() {
        batch.dispose();
        soldierTexture.dispose();
        enemyTexture.dispose();
        bulletTexture.dispose();
        muroTexture.dispose();
        font.dispose();
    }
}
