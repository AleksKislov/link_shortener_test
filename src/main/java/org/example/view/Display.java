package org.example.view;

import org.example.controller.LinkController;
import org.example.domain.Link;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public class Display extends JFrame {
    private final LinkController linkController;
    private final String linkBaseUrl;

    private final JLabel currentUserLabel;
    private final JButton startButton;
    private final JPanel mainPanel;
    private String currentUserId;

    public Display(LinkController linkController, String linkBaseUrl) {
        this.linkController = linkController;
        this.linkBaseUrl = linkBaseUrl;

        setTitle("Link shortener!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setSize(800, 600);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        add(mainPanel);
        startButton = new JButton("Выбор пользователя");
        mainPanel.add(startButton);
        startButton.addActionListener(e -> getAllUsers());

        currentUserLabel = new JLabel();
        clearCurrentUser();
        mainPanel.add(currentUserLabel);

        // для ввода существующей короткой ссылки
        JLabel shortLinkInputLabel = new JLabel("Перейти по короткой ссылке: " + linkBaseUrl + "... + [Enter]");
        mainPanel.add(shortLinkInputLabel);
        JTextField shortLinkInput = new JTextField();
        shortLinkInput.addActionListener(e -> processShortLinkInput(shortLinkInput.getText()));
        mainPanel.add(shortLinkInput);

        // для создания новой ссылки
        mainPanel.add(drawNewLinkInput());
    }

    private JPanel drawNewLinkInput() {
        JPanel newLinkPanel = new JPanel(new GridLayout(0, 2));
        newLinkPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel newLinkLabel = new JLabel("Новая ссылка:");
        newLinkPanel.add(newLinkLabel);
        JTextField newLinkInput = new JTextField(10);
        newLinkPanel.add(newLinkInput);

        JLabel clicksInputLabel = new JLabel("Лимит кликов:");
        newLinkPanel.add(clicksInputLabel);
        JTextField clicksInput = new JTextField(10);
        newLinkPanel.add(clicksInput);

        JLabel ttlInputLabel = new JLabel("TTL ссылки (сек):");
        newLinkPanel.add(ttlInputLabel);
        JTextField ttlInput = new JTextField(10);
        newLinkPanel.add(ttlInput);

        JLabel label = new JLabel("Задать новую ссылку: ");
        newLinkPanel.add(label);
        JButton saveLinkBtn = new JButton("Сохранить");
        saveLinkBtn.addActionListener(e -> saveNewLink(newLinkInput, clicksInput, ttlInput));
        newLinkPanel.add(saveLinkBtn);

        return newLinkPanel;
    }

    private void processShortLinkInput(String shortLink) {
        getAllUsers();
        Optional<Link> link = linkController.findByHash(shortLink);

        if (link.isEmpty()) {
            displayMessage("Короткая ссылка на найдена");
            return;
        }
        openLinkInBrowser(link.get(), false);
    }

    private void openLinkInBrowser(Link link, boolean displayUserLinks) {
        reduceClicksAmount(link, displayUserLinks);

        try {
            Desktop.getDesktop().browse(new URI(link.getOriginalLink()));
        } catch (IOException | URISyntaxException ex) {
            JOptionPane.showMessageDialog(this, "Не смогли открыть ссылку из-за: " + ex.getMessage());
        }
    }

    private void saveNewLink(JTextField linkInput, JTextField clicksInput, JTextField ttlInput) {
        String newLink = linkInput.getText();
        if (newLink.length() < 5) {
            displayMessage("Ссылка уже слишком короткая");
            return;
        }

        int clicksLimit = Integer.parseInt(clicksInput.getText());
        if (clicksLimit < 1) {
            displayMessage("Кол-во кликов должно быть больше 0");
            return;
        }

        int ttl = Integer.parseInt(ttlInput.getText());
        if (ttl < 1) {
            displayMessage("TTL ссылки должно быть больше 0");
            return;
        }

        Link link = linkController.createLink(newLink, clicksLimit, ttl, Optional.ofNullable(currentUserId));
        System.out.println(link);
        getUserLinks(link.getUserId());
    }

    private void clearCurrentUser() {
        currentUserLabel.setText("Текущий пользователь: " + null);
        currentUserId = null;
    }

    private void getAllUsers(){
        clear();
        Optional<Set<String>> userIds = linkController.findAllUsers();

        JPanel btnPanel = new JPanel(new GridLayout(0, 1));

        btnPanel.add(new JLabel("Выберете пользователя ниже"));

        JButton newUserButton = new JButton("Новый пользователь");
        newUserButton.addActionListener(e -> clearCurrentUser());
        btnPanel.add(newUserButton);

        if (userIds.isEmpty()) {
            btnPanel.add(new JLabel("Нет действующих пользователей"));
        } else {
             for (String userId : userIds.get()) {
                 JButton userButton = new JButton(userId);

                 userButton.addActionListener(e -> getUserLinks(userId));
                 btnPanel.add(userButton);
             }
        }

        add(btnPanel);
        redraw();
    }

    private void getUserLinks(String userId) {
        currentUserId = userId;
        currentUserLabel.setText("Текущий пользователь: " + userId);
        clear();
        Optional<ArrayList<Link>> links = linkController.findByUserId(userId);
        System.out.println(links);
        if (links.isEmpty() || links.get().isEmpty()) {
            getAllUsers();
        } else {
            for (Link link : links.get()) {
                drawLinkInfo(link);
            }
        }
    }

    private void clear() {
        for (Component comp : getContentPane().getComponents()) {
            if (comp != startButton && comp != mainPanel) remove(comp);
        }
    }

    private void redraw() {
        revalidate();
        repaint();
    }

    private void drawLinkInfo(Link link) {
        JPanel linkPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        linkPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        int clicksLeft = link.getClicksLimit();
        if (clicksLeft <= 0) {
            deleteLink(link, "Достигнут лимит по кликам: URL " + link.getOriginalLink(), true);
        }
        drawLinkLabel(linkPanel, "Кликов осталось", String.valueOf(clicksLeft));

        String origUri = link.getOriginalLink();
        drawLinkLabel(linkPanel, "Исходная ссылка", origUri);
        drawClickableLink(
                linkPanel,
                "Короткая ссылка",
                linkBaseUrl + link.getShortLink(),
                link
        );

        JLabel timeLabel = drawLinkLabel(linkPanel, "TTL", "- сек.");
        Timer timer = new Timer(1000, e -> updateLinkTimer(timeLabel, link));
        timeLabel.putClientProperty("timer", timer);
        timer.start();

        JButton deleteBtn = new JButton("Удалить ссылку");
        deleteBtn.addActionListener(e -> deleteLink(link, "ОK.", true));
        linkPanel.add(deleteBtn);

        add(linkPanel);
        redraw();
    }

    private JLabel drawLinkLabel(JPanel linkPanel, String txt, String value) {
        linkPanel.add(new JLabel(" " + txt + ":"));
        JLabel valueLabel = new JLabel(value);
        linkPanel.add(valueLabel);
        return valueLabel;
    }

    private void updateLinkTimer(JLabel timeLabel, Link link) {
        int ttl = (int) (link.getTtl() - (Instant.now().getEpochSecond() - link.getCreatedAt()));
        timeLabel.setText(ttl + " сек.");

        if (ttl <= 0) {
            ((Timer)timeLabel.getClientProperty("timer")).stop();
            deleteLink(link, "Время жизни ссылки истекло.", true);
        }
    }

    private void deleteLink(Link link, String msg, boolean displayUserLinks) {
        displayMessage(msg + " Запись удаляется");
        linkController.delete(link);

        if (displayUserLinks) getUserLinks(link.getUserId());
    }

    private void displayMessage(String txt) {
        JOptionPane.showMessageDialog(this, txt);
    }

    private void reduceClicksAmount(Link link, boolean displayUserLinks) {
        int clicksLeft = link.getClicksLimit() - 1;
        link.setClicksLimit(clicksLeft);
        linkController.update(link);

        if (clicksLeft > 0) return;

        deleteLink(link, "Достигнут лимит по кликам: URL " + link.getOriginalLink(), displayUserLinks);
    }

    private void drawClickableLink(JPanel linkPanel, String labelText, String uri, Link link) {
        JLabel linkLabel = drawLinkLabel(linkPanel, labelText, uri);

        if (link.getClicksLimit() <= 0) return;
        linkLabel.setForeground(Color.BLUE);
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (link.getClicksLimit() <= 0) return;

                openLinkInBrowser(link, true);
                getUserLinks(link.getUserId());
            }
        });
    }
}