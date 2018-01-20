package uk.co.gencoreoperative.btw.ui.panels;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import uk.co.gencoreoperative.btw.ActionFactory;
import uk.co.gencoreoperative.btw.ui.Context;
import uk.co.gencoreoperative.btw.ui.actions.ChooseMinecraftHome;
import uk.co.gencoreoperative.btw.ui.actions.ChoosePatch;
import uk.co.gencoreoperative.btw.ui.signals.MinecraftHome;

public class MinecraftHomePanel extends JPanel {

    private static final ImageIcon FOLDER = new ImageIcon(MinecraftHomePanel.class.getResource("/icons/folder.png"));

    public MinecraftHomePanel(Context context, ActionFactory actionFactory) {
        setBorder(new TitledBorder("Minecraft Home"));
        setLayout(new MigLayout(
                "fillx",
                "[min!][][min!]"));

        // Row 1
        final JLabel folderIcon = new JLabel(FOLDER);
        add(folderIcon);
        final JTextField homeTextField = new JTextField(20);
        homeTextField.setEnabled(true);
        homeTextField.setEditable(false);
        context.register(MinecraftHome.class, (o, arg) -> {
            String text = "";
            if (context.contains(MinecraftHome.class)) {
                text = context.get(MinecraftHome.class).getFolder().getAbsolutePath();
            }
            homeTextField.setText(text);
        });
        add(homeTextField, "grow");
        add(new JButton(new ChooseMinecraftHome(context, actionFactory)), "wrap");
    }
}