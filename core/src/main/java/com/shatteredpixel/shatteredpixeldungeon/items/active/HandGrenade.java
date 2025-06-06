package com.shatteredpixel.shatteredpixeldungeon.items.active;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class HandGrenade extends Grenade {
    {
        image = ItemSpriteSheet.GRENADE;

        max_amount = 1;
        amount = max_amount;
        dropChance = 0.125f;
    }

    @Override
    public int explodeMinDmg() {
        int dmg = super.explodeMinDmg();
        if (Dungeon.hero != null) {
            if (Dungeon.hero.hasTalent(Talent.MIYAKO_T1_1)) dmg += Dungeon.hero.pointsInTalent(Talent.MIYAKO_T1_1)+1;
        }
        return dmg;
    }

    @Override
    public int explodeMaxDmg() {
        int dmg = super.explodeMaxDmg();
        if (Dungeon.hero != null) {
            if (Dungeon.hero.hasTalent(Talent.MIYAKO_T1_1)) dmg += Dungeon.hero.pointsInTalent(Talent.MIYAKO_T1_1)+1;
        }
        return dmg;
    }

    @Override
    public Grenade.Boomer knockItem(){
        return new HandGrenadeBoomer();
    }

    public class HandGrenadeBoomer extends Boomer {

        {
            image = ItemSpriteSheet.GRENADE;
        }

        //needs to be overridden
        @Override
        protected void activate(int cell) {
            explode(cell);
        }

        @Override
        public int throwPos(Hero user, int dst) {
            if (Dungeon.level.distance(user.pos, dst) < 4) {
                switch (Dungeon.hero.pointsInTalent(Talent.MIYAKO_T3_1)) {
                    case 1:
                        return new Ballistica( user.pos, dst, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID ).collisionPos;
                    case 2:
                        return new Ballistica( user.pos, dst, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID ).collisionPos;
                    case 3:
                        return new Ballistica( user.pos, dst, Ballistica.STOP_TARGET ).collisionPos;
                    case 0: default:
                        return super.throwPos(user, dst);
                }
            } else {
                return super.throwPos(user, dst);
            }
        }
    }
}
