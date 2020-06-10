package net.llamadevelopment.friendsystem.components.forms.modal;

import cn.nukkit.Player;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.window.FormWindowModal;
import net.llamadevelopment.friendsystem.components.forms.FormHandler;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModalForm {

    private final String title, content, yes, no;
    private final Consumer<Player> onYes, onNo, closeCallback;
    private final BiConsumer<Player, FormResponseModal> submitCallback;

    public ModalForm(Builder b) {
        this.title = b.title;
        this.content = b.content;
        this.yes = b.yes;
        this.no = b.no;
        this.onYes = b.onYes;
        this.onNo = b.onNo;
        this.closeCallback = b.closeCallback;
        this.submitCallback = b.submitCallback;
    }

    public void send(Player player) {
        FormWindowModal form = new FormWindowModal(title, content, yes, no);

        FormHandler.modalPending.put(player.getName(), this);

        player.showFormWindow(form);
    }

    public void setYes(Player player) {
        onYes.accept(player);
    }

    public void setNo(Player player) {
        onNo.accept(player);
    }

    public void setClosed(Player player) {
        if (closeCallback == null) return;
        closeCallback.accept(player);
    }

    public void setSubmitted(Player p, FormResponseModal r) {
        if (submitCallback == null) return;
        submitCallback.accept(p, r);
    }

    public String getYes() {
        return yes;
    }

    public String getNo() {
        return no;
    }

    public static class Builder {

        private final String title, content, yes, no;
        private Consumer<Player> onYes, onNo, closeCallback;
        private BiConsumer<Player, FormResponseModal> submitCallback;

        public Builder(String title, String content, String yes, String no) {
            this.title = title;
            this.content = content;
            this.yes = yes;
            this.no = no;
        }

        public Builder onYes(Consumer<Player> cb) {
            onYes = cb;
            return this;
        }

        public Builder onNo(Consumer<Player> cb) {
            onNo = cb;
            return this;
        }

        public Builder onClose(Consumer<Player> cb) {
            closeCallback = cb;
            return this;
        }

        public Builder onSubmit(BiConsumer<Player, FormResponseModal> cb) {
            submitCallback = cb;
            return this;
        }

        public ModalForm build() {
            return new ModalForm(this);
        }
    }

}
