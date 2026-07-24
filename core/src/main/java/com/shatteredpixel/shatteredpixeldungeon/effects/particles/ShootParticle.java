package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import static com.shatteredpixel.shatteredpixeldungeon.actors.Char.INFINITE_ACCURACY;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RabbitSquadBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.SR.SR;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

public class ShootParticle extends SnipeParticle {
    public static Emitter.Factory factory(Char target, int tier, int lvl, Callback callback) {
        return new Emitter.Factory() {
            @Override
            public void emit(Emitter emitter, int index, float x, float y) {
                if (target == null) return; //타겟이 없으면 아무것도 하지 않음
                ((ShootParticle)emitter.recycle( ShootParticle.class )).reset( x, y, target, tier, lvl, callback );
            }
        };
    }

    public static Emitter.Factory factory(Char target, Gun.Bullet bullet, Callback callback) {
        return new Emitter.Factory() {
            @Override
            public void emit(Emitter emitter, int index, float x, float y) {
                ((ShootParticle)emitter.recycle( ShootParticle.class )).reset( x, y, target, bullet, callback );
            }
        };
    }

    public static Emitter.Factory enhancedFactory(Char target, KindOfWeapon heroWep, Callback callback) {
        return new Emitter.Factory() {
            @Override
            public void emit(Emitter emitter, int index, float x, float y) {
                ((ShootParticle)emitter.recycle( ShootParticle.class )).reset( x, y, target, heroWep, callback );
            }
        };
    }

    public ShootParticle() {
        super();
        color(0x000000);
        am = 0;
    }

    boolean shoot; //총 발사 여부
    Char target = null;
    int tier = -1;
    int lvl = -1;
    Gun.Bullet bullet = null;
    Callback callback;

    boolean enhanceFlag;
    KindOfWeapon heroWep;

    //총 발사 후 쉬는 구간 이후 마지막 구간 사이는 아무것도 하지 않음

    public void reset( float x, float y, Char target, int tier, int lvl, Callback callback) {
        reset(x, y);

        size(2f);

        shoot = false;

        this.target = target;
        this.tier = tier;
        this.lvl = lvl;
        this.callback = callback;
        this.enhanceFlag = false;
    }

    private void reset(float x, float y, Char target, KindOfWeapon heroWep, Callback callback) {
        reset(x, y);

        size(2f);

        shoot = false;

        this.enhanceFlag = true;
        this.target = target;
        this.heroWep = heroWep;
        this.callback = callback;
    }

    public void reset(float x, float y, Char target, Gun.Bullet bullet, Callback callback) {
        reset(x, y);

        size(2f);

        shoot = false;

        this.target = target;
        this.bullet = bullet;
        this.callback = callback;
    }

    @Override
    public void update() {
        super.update();

        if (!shoot && left <= lifespan- FIRST - MIDDLE_REST) { //left가 총 발사 타이밍과 완벽하게 일치하지 않아서 범위로 지정
            Gun.Bullet attackingBullet;
            if (bullet != null) {
                attackingBullet = bullet;
            } else {
                Gun gun = Gun.getGun(SR.class, this.tier, this.lvl);
                if(SPDSettings.rabbitEnhance()){
                    if(Dungeon.hero.belongings.weapon!=null && Dungeon.hero.belongings.weapon instanceof Gun){
                        Gun temp = (Gun) Dungeon.hero.belongings.weapon;
                        gun.enchantment          = temp.enchantment;
                        gun.curseInfusionBonus   = temp.curseInfusionBonus;
                        gun.masteryPotionBonus   = temp.masteryPotionBonus;
                        gun.levelKnown           = temp.levelKnown;
                        gun.cursedKnown          = temp.cursedKnown;
                        gun.cursed               = temp.cursed;
                        gun.augment              = temp.augment;
                        gun.enchantHardened      = temp.enchantHardened;
                        gun.keptThoughLostInvent = temp.keptThoughLostInvent;
                        gun.barrelMod = temp.barrelMod;
                        gun.magazineMod = temp.magazineMod;
                        gun.bulletMod = temp.bulletMod;
                        gun.weightMod = temp.weightMod;
                        gun.attachMod = temp.attachMod;
                        gun.enchantMod = temp.enchantMod;
                        gun.inscribeMod = temp.inscribeMod;
                    }
                }
                attackingBullet = gun.knockBullet();
                if (Dungeon.hero.hasTalent(Talent.MIYAKO_EX1_2)) {
                    if(!SPDSettings.rabbitEnhance()){
                        attackingBullet.setAccMulti(1f+(2f*Dungeon.hero.pointsInTalent(Talent.MIYAKO_EX1_2)-1f));
                    }
                    else {
                        attackingBullet.setAccMulti(INFINITE_ACCURACY);
                        Buff.affect(Dungeon.hero,RabbitSquadBuff.MiyuDmgEnhance.class);
                    }
                }
            }

            attackingBullet.throwSound();
            attackingBullet.shoot(this.target.pos, true);
            CellEmitter.center(this.target.pos).burst(BlastParticle.FACTORY, 4);
            shoot = true;
        }

        if (left <= 0) {
            this.callback.call();
        }
    }
}
