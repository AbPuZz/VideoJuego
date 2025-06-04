package diana.ismaFabio;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private FitViewport viewport;

    private Texture ninjaTexture;
    private Texture muroTexture;

    private Sprite ninjaSprite;

    private final int TILE_SIZE = 64; // Tamaño del tile (ajusta según tu imagen)

    // Laberinto: 1 = muro, 0 = espacio vacío
    private int[][] maze = {
        {1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,1,0,0,0,0,1},
        {1,0,1,0,1,0,1,1,0,1},
        {1,0,1,0,0,0,0,1,0,1},
        {1,0,1,1,1,1,0,1,0,1},
        {1,0,0,0,0,1,0,0,0,1},
        {1,1,1,1,0,1,1,1,0,1},
        {1,0,0,1,0,0,0,1,0,1},
        {1,0,0,0,0,1,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1},
    };

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new FitViewport(840, 680);

        ninjaTexture = new Texture("pixil-frame-0 (1).png");
        muroTexture = new Texture("muro.png");

        ninjaSprite = new Sprite(ninjaTexture);
        ninjaSprite.setPosition(TILE_SIZE + 5, TILE_SIZE + 5); // Posición inicial
        ninjaSprite.setSize(TILE_SIZE * 0.8f, TILE_SIZE * 0.8f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private void input() {
        float speed = 200f;
        float delta = Gdx.graphics.getDeltaTime();

        float oldX = ninjaSprite.getX();
        float oldY = ninjaSprite.getY();

        float newX = oldX;
        float newY = oldY;

        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            newX += speed * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            newX -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            newY += speed * delta;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            newY -= speed * delta;
        }

        // Limitar dentro del mapa
        float mapWidth = maze[0].length * TILE_SIZE;
        float mapHeight = maze.length * TILE_SIZE;

        newX = Math.max(0, Math.min(newX, mapWidth - ninjaSprite.getWidth()));
        newY = Math.max(0, Math.min(newY, mapHeight - ninjaSprite.getHeight()));

        ninjaSprite.setPosition(newX, newY);

        if (isCollidingWithWall(ninjaSprite)) {
            ninjaSprite.setPosition(oldX, oldY);
        }
    }


    // Función para detectar colisión entre el ninja y las paredes del laberinto
    private boolean isCollidingWithWall(Sprite sprite) {
        // Obtenemos el rectángulo del sprite
        float spriteX = sprite.getX();
        float spriteY = sprite.getY();
        float spriteWidth = sprite.getWidth();
        float spriteHeight = sprite.getHeight();

        // Revisamos todas las celdas que el sprite puede estar tocando
        // Calculamos los índices de las celdas que el sprite ocupa
        int leftTile = (int) (spriteX / TILE_SIZE);
        int rightTile = (int) ((spriteX + spriteWidth) / TILE_SIZE);
        int bottomTile = (int) (spriteY / TILE_SIZE);
        int topTile = (int) ((spriteY + spriteHeight) / TILE_SIZE);

        for (int row = bottomTile; row <= topTile; row++) {
            for (int col = leftTile; col <= rightTile; col++) {
                // Verificamos límites del array
                if (row < 0 || row >= maze.length || col < 0 || col >= maze[0].length) {
                    return true; // Fuera del laberinto se considera muro
                }
                if (maze[maze.length - 1 - row][col] == 1) {
                    // Si en esa celda hay muro, colisiona
                    return true;
                }
            }
        }

        return false; // No colisiona con ningún muro
    }


    private void draw() {
        ScreenUtils.clear(Color.WHITE);
        viewport.apply();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // Dibujamos el laberinto
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[row].length; col++) {
                if (maze[row][col] == 1) {
                    batch.draw(muroTexture, col * TILE_SIZE, (maze.length - 1 - row) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Dibujamos al ninja
        ninjaSprite.draw(batch);

        batch.end();
    }

    @Override
    public void render() {
        input();
        draw();
    }

    @Override
    public void dispose() {
        batch.dispose();
        ninjaTexture.dispose();
        muroTexture.dispose();
    }
}
