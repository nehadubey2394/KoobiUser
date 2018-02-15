package views.niftynotification.lib;

import views.niftynotification.lib.effects.BaseEffect;
import views.niftynotification.lib.effects.Flip;
import views.niftynotification.lib.effects.Jelly;
import views.niftynotification.lib.effects.Scale;
import views.niftynotification.lib.effects.SlideIn;
import views.niftynotification.lib.effects.SlideOnTop;
import views.niftynotification.lib.effects.Standard;
import views.niftynotification.lib.effects.ThumbSlider;


public enum Effects {
    standard(Standard.class),
    slideOnTop(SlideOnTop.class),
    flip(Flip.class),
    slideIn(SlideIn.class),
    jelly(Jelly.class),
    thumbSlider(ThumbSlider.class),
    scale(Scale.class);


    private Class<? extends BaseEffect> effectsClazz;

    private Effects(Class<? extends BaseEffect> mclass) {
        effectsClazz = mclass;
    }

    public BaseEffect getAnimator() {
        BaseEffect bEffects=null;
        try {
            bEffects = effectsClazz.newInstance();
        } catch (ClassCastException e) {
            throw new Error("Can not init animatorClazz instance");
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            throw new Error("Can not init animatorClazz instance");
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            throw new Error("Can not init animatorClazz instance");
        }
        return bEffects;
    }
}
