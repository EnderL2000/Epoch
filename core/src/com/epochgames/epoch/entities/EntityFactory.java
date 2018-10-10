package com.epochgames.epoch.entities;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.epochgames.epoch.Epoch;
import com.epochgames.epoch.GameManager;
import com.epochgames.epoch.entities.components.*;
import com.epochgames.epoch.screens.InGame;
import com.epochgames.epoch.util.Assets;
import com.epochgames.epoch.util.HexagonGrid;
import com.epochgames.epoch.util.hexlib.HexSatelliteData;
import com.epochgames.epoch.util.hexlib.Hexagon;
import com.epochgames.epoch.util.hexlib.OffsetCoord;
import org.codetome.hexameter.core.api.CubeCoordinate;
import org.codetome.hexameter.core.api.HexagonalGrid;

/**
 * This is the Big Kahuna since this in the class that will
 * populate our universe with entities. Somehow it'll have to take into account
 * story but hey we'll get there /shrug
 */
public class EntityFactory {
    public static Epoch game;

    //Boolean to determine whether or not the factory has been initialized
    private static boolean initialized = false;

    //Sets the game only if the factory has not been initialized already
    public static void init(Epoch game) {
        if(!initialized) {
            EntityFactory.game = game;

            initialized = true;
        }
    }

    /**
     * Creates an NPC/Player ship at the specified position
     * @param coordinates The cube Coordinates of where to create the ship
     * @param ship The ship to create
     * @param isPlayer Whether or not this Entity is a player
     * @return The created entity
     */
    public static Entity createShip(CubeCoordinate coordinates, HexagonGrid hexagonGrid, Ship ship, boolean isPlayer) {
        Entity newShip = new Entity();

        ActionCompletenessComponent actionCompletenessComponent = new ActionCompletenessComponent();
        //If not a player, should get distance from player every turn. If the NPC is in view of the player then this becomes true
        actionCompletenessComponent.actionCompleteness = isPlayer ?  ActionCompletenessComponent.FULL : ActionCompletenessComponent.NONE;

        HealthComponent healthComponent = new HealthComponent();
        healthComponent.health = ship.shipMake.getHealth();
        healthComponent.armor = ship.shipMake.getArmor();

        IconComponent iconComponent = new IconComponent();
        iconComponent.region = Assets.MANAGER.get(Assets.Spritesheets.SHIPS).findRegion(ship.shipMake.getAtlasRegion());

        TransformComponent transformComponent = new TransformComponent();
        transformComponent.scale = iconComponent.region.getRegionHeight() < iconComponent.region.getRegionWidth() ?
                (float)GameManager.SPRITE_SIZE / (float)iconComponent.region.getRegionHeight() :
                (float)GameManager.SPRITE_SIZE / (float)iconComponent.region.getRegionWidth();
        transformComponent.rotation = 0.0f;
        //HexagonGrid c = game.inGameScreen.hexagonGrid;
        transformComponent.position = new Vector2(
                (float)hexagonGrid.hexGrid.getByCubeCoordinate(coordinates).get().getCenterX(),
                (float)hexagonGrid.hexGrid.getByCubeCoordinate(coordinates).get().getCenterY());

        MoveComponent moveComponent = new MoveComponent();

        TurnComponent turnComponent = new TurnComponent();

        TypeComponent typeComponent = new TypeComponent();
        if(!isPlayer) {
            if(ship.isPirate) {
                typeComponent.type = TypeComponent.ENEMY; //NPC is a pirate
                turnComponent.priority = TurnComponent.PIRATE_PRIORITY;
            }
            else {
                typeComponent.type = TypeComponent.NEUTRAL; //NPC is not a pirate
                turnComponent.priority = TurnComponent.ENEMY_PRIORITY;
            }
        }
        else {
            turnComponent.priority = TurnComponent.PLAYER_PRIORITY;
            typeComponent.type = TypeComponent.PLAYER; //NPC is actually the player
        }

        newShip.add(actionCompletenessComponent).add(healthComponent).add(iconComponent).
                add(transformComponent).add(turnComponent).
                add(typeComponent).add(moveComponent);

        return newShip;
    }
}
