package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.nonomi;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class BlackCard extends ArmorAbility {
    {
        baseChargeUse = 35f;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {
        armor.charge -= chargeUse(hero);
        armor.updateQuickslot();
        Invisibility.dispel();
        float damagePerGold = 30f - 5f * hero.pointsInTalent(Talent.NONOMI_ARMOR2_1);
        int damage = (int)(Dungeon.gold/damagePerGold);

        if (hero.hasTalent(Talent.NONOMI_ARMOR2_3)) {
            Buff.affect(hero, Light.class, Math.round(Dungeon.gold/(5f-hero.pointsInTalent(Talent.NONOMI_ARMOR2_3))));
        }

        hero.sprite.showStatusWithIcon( CharSprite.NEGATIVE, Integer.toString(-Dungeon.gold), FloatingText.GOLD );
        Dungeon.gold = 0;

        for (Mob mob : Actor.enemiesInHeroFOV(Dungeon.level)) {
            mob.damage(damage, hero);
            if (hero.hasTalent(Talent.NONOMI_ARMOR2_2) && !mob.isAlive()) {
                new Gold().quantity(20*hero.pointsInTalent(Talent.NONOMI_ARMOR2_2)).doPickUp(hero);
            }
        }

        hero.sprite.operate(hero.pos);
        hero.spendAndNext(1f);
    }

    @Override
    public int icon() {
        return HeroIcon.NONOMI_2;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.NONOMI_ARMOR2_1, Talent.NONOMI_ARMOR2_2, Talent.NONOMI_ARMOR2_3, Talent.HEROIC_ENERGY};
    }
}
