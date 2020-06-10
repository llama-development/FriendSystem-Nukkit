package net.llamadevelopment.friendsystem.components.forms;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import net.llamadevelopment.friendsystem.components.forms.custom.CustomForm;
import net.llamadevelopment.friendsystem.components.forms.modal.ModalForm;
import net.llamadevelopment.friendsystem.components.forms.simple.SimpleForm;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FormHandler {

    public static HashMap<String, SimpleForm> simplePending = new HashMap<>();
    public static HashMap<String, ModalForm> modalPending = new HashMap<>();
    public static HashMap<String, CustomForm> customPending = new HashMap<>();

    public static void handleSimple(Player player, FormWindowSimple form) {
        if (simplePending.containsKey(player.getName())) {
            SimpleForm sform = simplePending.get(player.getName());
            simplePending.remove(player.getName());

            if (form.getResponse() == null) {
                sform.setClosed(player);
                return;
            }

            ElementButton clickedButton = form.getResponse().getClickedButton();

            for (Map.Entry<ElementButton, Consumer<Player>> map : sform.getButtons().entrySet()) {
                if (map.getKey().getText().equalsIgnoreCase(clickedButton.getText())) {
                    if (map.getValue() != null) map.getValue().accept(player);
                    break;
                }
            }

            sform.setSubmitted(player, form.getResponse());

        }
    }

    public static void handleModal(Player player, FormWindowModal form) {
        if (modalPending.containsKey(player.getName())) {
            ModalForm mform = modalPending.get(player.getName());
            modalPending.remove(player.getName());

            if (form.getResponse() == null) {
                mform.setClosed(player);
                return;
            }

            String clickedButton = form.getResponse().getClickedButtonText();
            if (clickedButton.equalsIgnoreCase(mform.getYes())) mform.setYes(player);
            if (clickedButton.equalsIgnoreCase(mform.getNo())) mform.setNo(player);

            mform.setSubmitted(player, form.getResponse());
        }
    }

    public static void handleCustom(Player player, FormWindowCustom form) {
        if (customPending.containsKey(player.getName())) {
            CustomForm cform = customPending.get(player.getName());
            customPending.remove(player.getName());

            if (form.getResponse() == null) {
                cform.setClosed(player);
                return;
            }

            cform.setSubmitted(player, form.getResponse());
        }
    }

}
