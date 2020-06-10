package net.llamadevelopment.friendsystem.components.forms.custom;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.Element;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import net.llamadevelopment.friendsystem.FriendSystem;
import net.llamadevelopment.friendsystem.components.forms.FormHandler;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CustomForm {

    private final String title;
    private final ArrayList<Element> elements;
    private final Consumer<Player> closeCallback;
    private final BiConsumer<Player, FormResponseCustom> submitCallback;

    public CustomForm(Builder b) {
        this.title = b.title;
        this.elements = b.elements;
        this.closeCallback = b.closeCallback;
        this.submitCallback = b.submitCallback;
    }


    public void send(Player player) {
        FormWindowCustom form = new FormWindowCustom(title);
        elements.forEach(form::addElement);

        FormHandler.customPending.put(player.getName(), this);
        player.showFormWindow(form);

        Server.getInstance().getScheduler().scheduleDelayedTask(FriendSystem.getInstance(), () -> player.sendExperience(player.getExperience()), 20);
    }

    public void setClosed(Player player) {
        if (closeCallback == null) return;
        closeCallback.accept(player);
    }

    public void setSubmitted(Player player, FormResponseCustom form) {
        submitCallback.accept(player, form);
    }

    public static class Builder {

        private final String title;
        private ArrayList<Element> elements = new ArrayList<>();
        private Consumer<Player> closeCallback;
        private BiConsumer<Player, FormResponseCustom> submitCallback;

        public Builder(String title) {
            this.title = title;
        }

        public Builder addElement(Element element) {
            elements.add(element);
            return this;
        }

        public Builder onClose(Consumer<Player> cb) {
            this.closeCallback = cb;
            return this;
        }

        public Builder onSubmit(BiConsumer<Player, FormResponseCustom> cb) {
            this.submitCallback = cb;
            return this;
        }


        public CustomForm build() {
            return new CustomForm(this);
        }

    }

}
