package org.example.visualnovelapp;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    public class ChoiceOption {
        public final String text;
        public final Runnable action;

        public ChoiceOption(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
    }


    // --- Компоненты GUI ---

    private void animateText(String text) {
        mainText.setText(""); // Очищаем текст перед началом анимации
        mainText.setOpacity(0); // Устанавливаем начальную прозрачность

        // Анимация затухания (появление)
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), mainText);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Эффект печатной машинки
        SequentialTransition typewriter = new SequentialTransition();
        for (int i = 0; i < text.length(); i++) {
            String partialText = text.substring(0, i + 1);
            PauseTransition pause = new PauseTransition(Duration.millis(20)); // Задержка между символами
            pause.setOnFinished(event -> mainText.setText(partialText));
            typewriter.getChildren().add(pause);
        }

        // Запускаем анимации последовательно
        SequentialTransition sequence = new SequentialTransition(fadeIn, typewriter);
        sequence.play();
    }
    private MediaPlayer backgroundMusicPlayer;
    private MediaPlayer soundEffectPlayer;
    private StackPane rootPane; // Корневой контейнер
    private ImageView backgroundView; // Для фона
    private ImageView characterView;  // Для спрайта персонажа
    private VBox dialogueBox;       // Контейнер для текста и имени
    private Label speakerLabel;     // Имя говорящего
    private Text mainText;          // Основной текст (диалог, мысли)
    private VBox choiceBox;         // Контейнер для кнопок выбора

    // --- Состояние игры (твои переменные) ---
    private boolean karinaLikesYou = false;
    private boolean hasSpoon = false;
    private boolean isRat = false;

    // --- Управление потоком игры ---
    private List<Runnable> currentSceneSteps; // Шаги текущей сцены
    private int currentStepIndex;             // Индекс текущего шага

    // --- Метод для создания UI (вместо FXML для примера) ---
    public Parent createUI() {
        rootPane = new StackPane();



        backgroundView = new ImageView();
        backgroundView.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundView.fitHeightProperty().bind(rootPane.heightProperty());
        backgroundView.setPreserveRatio(false);

        characterView = new ImageView();
        characterView.setFitHeight(450);
        characterView.setPreserveRatio(true);
        StackPane.setAlignment(characterView, Pos.BOTTOM_CENTER);
        characterView.setTranslateY(-50);

        // Диалоговое окно

        dialogueBox = new VBox(10);
        dialogueBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");
        dialogueBox.setPadding(new Insets(15));
        dialogueBox.setMaxHeight(200);
        dialogueBox.setMaxWidth(750);

        speakerLabel = new Label("Имя");
        speakerLabel.setFont(new Font("Arial Bold", 18));
        speakerLabel.setTextFill(Color.WHITE);

        mainText = new Text("Текст диалога...");
        mainText.setFont(new Font("Comic sans MS", 16));
        mainText.setFill(Color.WHITE);
        mainText.setWrappingWidth(720);

        dialogueBox.getChildren().addAll(speakerLabel, mainText);
        StackPane.setAlignment(dialogueBox, Pos.BOTTOM_CENTER);
        dialogueBox.setTranslateY(-20);


        // Контейнер для кнопок выбора
        choiceBox = new VBox(10);
        choiceBox.setAlignment(Pos.CENTER);
        choiceBox.setMaxWidth(300);
        StackPane.setAlignment(choiceBox, Pos.CENTER);

        rootPane.getChildren().addAll(backgroundView, characterView, dialogueBox, choiceBox);

        dialogueBox.setOnMouseClicked(event -> nextStep());

        showTitleScreen();

        return rootPane;
    }

    private void playBackgroundMusic(String audioFile) {
        try {
            // Останавливаем предыдущую музыку, если она играет
            if (backgroundMusicPlayer != null) {
                backgroundMusicPlayer.stop();
            }

            URL resource = getClass().getResource("/audio/" + audioFile);
            if (resource == null) {
                System.err.println("Аудиофайл не найден: " + audioFile);
                return;
            }

            Media media = new Media(resource.toString());
            backgroundMusicPlayer = new MediaPlayer(media);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Бесконечное повторение
            backgroundMusicPlayer.play();
        } catch (Exception e) {
            System.err.println("Ошибка загрузки аудио: " + audioFile);
            e.printStackTrace();
        }
    }

    private void playSoundEffect(String audioFile) {
        try {
            // Останавливаем предыдущий эффект, если он играет
            if (soundEffectPlayer != null) {
                soundEffectPlayer.stop();
            }

            URL resource = getClass().getResource("/audio/" + audioFile);
            if (resource == null) {
                System.err.println("Аудиофайл эффекта не найден: " + audioFile);
                return;
            }

            Media media = new Media(resource.toString());
            soundEffectPlayer = new MediaPlayer(media);
            soundEffectPlayer.play();
        } catch (Exception e) {
            System.err.println("Ошибка загрузки аудио эффекта: " + audioFile);
            e.printStackTrace();
        }
    }

    private void stopAllAudio() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }
        if (soundEffectPlayer != null) {
            soundEffectPlayer.stop();
        }
    }

    private void setBackground(String imageName) {
        try {
            System.out.println("[Загрузка фона:" + imageName + "]");

            InputStream is = getClass().getResourceAsStream("/images/" + imageName);
            if (is == null) {
                System.err.println("Файл не найден: /images/" + imageName);
                backgroundView.setImage(null);
                return;
            }

            Image img = new Image(is);
            backgroundView.setImage(img);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCharacter(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            characterView.setImage(null);
            characterView.setVisible(false);
            return;
        }
        try {
            System.out.println("[Загрузка персонажа: " + imageName + "]");
            InputStream is = getClass().getResourceAsStream("/images/" + imageName);
            if (is == null) {
                System.err.println("Файл не найден: /images/" + imageName);
                characterView.setImage(null);
                characterView.setVisible(false);
                return;
            }
            Image img = new Image(is);
            characterView.setImage(img);
            characterView.setVisible(true);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки персонажа: " + imageName);
            e.printStackTrace();
            characterView.setImage(null);
            characterView.setVisible(false);
        }
    }

    private void showDialogue(String speaker, String text) {
        dialogueBox.setVisible(true);
        choiceBox.setVisible(false);
        speakerLabel.setText(speaker);
        mainText.setStyle("-fx-font-style: normal;");
        animateText(text); // Запускаем анимацию текста
    }

    private void showThoughts(String text) {
        dialogueBox.setVisible(true);
        choiceBox.setVisible(false);
        speakerLabel.setText("Мысли");
        mainText.setStyle("-fx-font-style: italic;");
        animateText(text); // Запускаем анимацию текста
    }

    private void showChoices(List<ChoiceOption> options) {
        dialogueBox.setVisible(false);
        choiceBox.getChildren().clear();
        choiceBox.setVisible(true);

        Label title = new Label("ВАШИ ДЕЙСТВИЯ:");
        title.setFont(new Font("Arial Bold", 18));
        title.setTextFill(Color.ORANGE);
        choiceBox.getChildren().add(title);

        for (ChoiceOption option : options) {
            Button button = new Button(option.text);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(event -> {
                choiceBox.setVisible(false);
                option.action.run();
            });
            choiceBox.getChildren().add(button);
        }
    }



    private void startScene(List<Runnable> steps) {
        currentSceneSteps = steps;
        currentStepIndex = 0;
        nextStep();
    }

    private void nextStep() {
        if (choiceBox.isVisible()) {
            return;
        }
        if (currentSceneSteps == null || currentStepIndex >= currentSceneSteps.size()) {
            System.out.println("Нет следующего шага.");
            return;
        }

        Runnable currentAction = currentSceneSteps.get(currentStepIndex);
        currentAction.run();

        currentStepIndex++;
    }

    private void showTitleScreen() {
        setBackground("title_background.png");
        setCharacter(null);
        playBackgroundMusic("title_theme.mp3");
        choiceBox.setVisible(false);

        dialogueBox.setVisible(true);
        speakerLabel.setText(" ");

        String titleText =
                "тюрьма для несовершеннолетних. 2025 год.\n\n" +
                        "Мирон Якович Федоров, 31.01.1985 г.р.\n" +
                        "Осужден по статье 134 УК РФ к пожизненному заключению\n" +
                        "в детской колонии строгого режима по ошибке...\n\n" +
                        "(Кликните, чтобы продолжить)";

        mainText.setText(titleText);
        mainText.setTextAlignment(TextAlignment.CENTER);

        currentSceneSteps = List.of(this::prisonCellScene);
        currentStepIndex = 0;
    }

    // --- СЦЕНА 1: Камера ---
    private void prisonCellScene() {
        List<Runnable> steps = new ArrayList<>();

        steps.add(() -> {
            setBackground("OXICHILD.jpg");

            showThoughts("Вот черт, и угораздило же...\nОдин из лучших студентов Оксфорда сидит\nв тюрьме для несовершеннолетних на пожизненном.\nНадо сбежать!!");
        });

        steps.add(() -> {
            setCharacter("");
            showDialogue("Подросток", "Здорова, брат.");
        });

        steps.add(() -> {
            List<ChoiceOption> choices = List.of(
                    new ChoiceOption("А. Привет", () -> handlePrisonGreeting(1)),
                    new ChoiceOption("Б. Мир вашему дому", () -> handlePrisonGreeting(2)),
                    new ChoiceOption("В. Доброе утречко, друзья", () -> handlePrisonGreeting(3))
            );
            showChoices(choices);
        });

        startScene(steps);
    }

    private void handlePrisonGreeting(int choice) {
        List<Runnable> steps = new ArrayList<>();
        if (choice == 2) {
            steps.add(() -> {
                setBackground("podrost.png");
                showDialogue("Подросток", "За что попал, брат?");
            });
        } else {
            steps.add(() -> {
                setBackground("maboy.jpg");
                showDialogue("Подросток", "Выбирай слова, петух.\nЗа что попал сюда?");
            });
        }

        steps.add(() -> {
            List<ChoiceOption> choices = List.of(
                    new ChoiceOption("А. 134 УК РФ", () -> handlePrisonArticle(1)),
                    new ChoiceOption("Б. Тебя волнует или интересует?", () -> handlePrisonArticle(2)),
                    new ChoiceOption("В. За половые связи с несовершеннолетними", () -> handlePrisonArticle(3))
            );
            showChoices(choices);
        });

        startScene(steps);
    }

    private void handlePrisonArticle(int choice) {
        List<Runnable> steps = new ArrayList<>();

        steps.add(() -> {
            setBackground("maboy.jpg");
            showDialogue("Подростки", "ТАК ТЫ ИХ ЭТИХ, ПЕТУХ!\nТяжко тебе будет здесь,\nвыглядишь как дед и малолеток любишь!\nВся тюрьма тебя отлюбит.");
        });

        steps.add(() -> {
            setBackground("duma.jpg");
            showThoughts("Нужно придумать план побега...");
        });

        steps.add(this::canteenFirstScene);

        startScene(steps);
    }

    // --- СЦЕНА 2: Столовая (Первая встреча) ---
    private void canteenFirstScene() {
        List<Runnable> steps = new ArrayList<>();
        steps.add(() -> {
            setBackground("DINNEROX.jpg");
            setCharacter("karina_neutral.png");
            showDialogue("Карина", "Привет, я слышала о тебе, петушок)");
        });
        steps.add(() -> {
            setCharacter("miron_neutral.png");
            showDialogue("Мирон", "Приятно слышать) Как тебя зовут?");
        });
        steps.add(() -> {
            setBackground("sukazlaya.jpg");
            showDialogue("Карина", "Меня зовут Карина. Как зовут тебя - мне без разницы.\nЛибо отдаешь свою порцию еды,\nлибо в душе, когда уронишь мыло, тебе не поздоровится..");
        });
        steps.add(() -> {
            setBackground("zlabluad.jpg");
            List<ChoiceOption> choices = List.of(
                    new ChoiceOption("А. Отдать еду", () -> handleCanteenChoice1(1)),
                    new ChoiceOption("Б. Съесть самому", () -> handleCanteenChoice1(2))
            );
            showChoices(choices);
        });

        startScene(steps);
    }

    private void handleCanteenChoice1(int choice) {
        List<Runnable> steps = new ArrayList<>();
        if (choice == 1) {
            steps.add(() -> {
                setBackground("zlogrus.jpg");
                showDialogue("Мирон", "Вот... держи мою порцию...\nнадеюсь, она тебе понравится.");
            });
            steps.add(() -> {
                setBackground("DINNEROX.jpg");
                showDialogue("Карина", "Ты правильно поступил, петушочек)");
            });
            steps.add(() -> {
                setBackground("zlogrus.jpg");
                showThoughts("Сегодня я ходил голодный и без настроения...\nНадо было съесть самому.");
            });
        } else {
            steps.add(() -> {
                setBackground("chill.jpg");
                showDialogue("Мирон", "Петушара жирная! Ешь свое месиво,\nтолько не плачь потом!");
            });
            steps.add(() -> {
                setCharacter("tualetka.jpg");
                showThoughts("Через 3 часа живот скрутило...\nСледующие несколько часов не слезал с туалета.\nНадо было отдать ей это долбаное хрючево...");
            });
        }
        steps.add(this::corridorFirstScene);
        startScene(steps);
    }

    // --- СЦЕНА 3: Коридор (Первая встреча) ---
    private void corridorFirstScene() {
        List<Runnable> steps = new ArrayList<>();
        steps.add(() -> {
            setBackground("koridor.jpg");
            setCharacter("miron_thinking.png");
            showThoughts("Я кот в стае собак...\nЯ кокаин в толпе негров...");
        });
        steps.add(() -> {
            setCharacter("nikita_neutral.png");
            showDialogue("Никита", "Привет, брат. Как тебя зовут?");
        });
        steps.add(() -> {
            setCharacter("miron_neutral.png");
            showDialogue("Мирон", "Здарова, брат. Меня зовут Мирон.\nА тебя как?");
        });
        steps.add(() -> {
            setCharacter("nikita_neutral.png");
            showDialogue("Никита", "Никита Повитухов. А тебя Оксимирон значит.\nВот ответь мне на вопрос,\nпочему нет игры, где ты типа живешь спокойную жизнь,\nработаешь на работе и ты типа обычный человек?");
        });
        steps.add(() -> {
            setCharacter("miron_neutral.png");
            showDialogue("Никита", "Не знаю, брат. Не для нас такая жизнь...\nТы новичок здесь, привыкнешь еще, не переживай.");
        });
        steps.add(() -> {
            setCharacter("nikita_sad.png");
            showDialogue("Мирон", "Пожизненно мне сидеть,\nконечно надо привыкнуть...\nНавальный сидел, и я буду теперь.");
        });
        steps.add(() -> {
            setCharacter("miron_neutral.png");
            showDialogue("Никита", "Навальный умер.\nЕсли захочешь облегчить себе срок - подходи ко мне,\nпомогу тебе.");
        });
        steps.add(() -> {
            setCharacter("nikita_neutral.png");
            showDialogue("Мирон", "Спасибо тебе.");
        });
        steps.add(() -> {
            setCharacter("miron_thinking.png");
            showThoughts("Неужели он знает выход?\nПочему он вызывает у меня такое доверие?");
        });
        steps.add(this::canteenSecondScene);
        startScene(steps);
    }

    private void canteenSecondScene() {
        List<Runnable> steps = new ArrayList<>();

        steps.add(() -> {
            setBackground("DINNEROX.jpg");

            showThoughts("Здарова петушок");
        });

        steps.add(() -> {
            setCharacter("");
        });

        steps.add(() -> {
            List<ChoiceOption> choices = List.of(
                    new ChoiceOption("А. Поцеловать Карину", () -> handleCanteenChoice2(1)),
                    new ChoiceOption("Б. Отдать еду", () -> handleCanteenChoice2(2))
            );
            showChoices(choices);
        });

        startScene(steps);
    }

    private void handleCanteenChoice2(int choice) {
        List<Runnable> steps = new ArrayList<>();
        karinaLikesYou = true;

        if (choice == 1) {
            steps.add(() -> {
                setBackground("luba.jpg");
                showDialogue("Мирон", "(Вы целуете Карину без разрешения!)");
            });
            steps.add(() -> {
                setBackground("shok.jpg");
                showDialogue("Карина", "Целуй меня сколько хочешь,\nно еду, чухан, отдавать мне должен!");
            });
        } else {
            steps.add(() -> {
                setBackground("DINNEROX.jpg");
                showDialogue("Карина", "Такой послушненький...");
            });
            steps.add(() -> {
                setBackground("eda.jpg");
                showDialogue("Мирон", "Еда в твоем рту чуть отодвинулась в сторону...");
            });
            steps.add(() -> {
                setBackground("DINNEROX.jpg");
                showDialogue("Карина", "За то что отдал еду, ты мне должен!\nЛадно, целуй меня сколько хочешь, но только за еду.");
            });
            steps.add(() -> {

                showDialogue("Мирон", "Я не расстроен - еда тут все равно отстойная.");
            });
        }

        steps.add(this::showerScene);
        startScene(steps);
    }

    // --- СЦЕНА 5: Душевая ---
    private void showerScene() {
        List<Runnable> steps = new ArrayList<>();
        steps.add(() -> {
            setBackground("BATHOX.jpg");

            showThoughts("Даже не верится, сколько времени я уже здесь...\nПора в душ.");
        });
        steps.add(() -> {
            setCharacter("miron_content.png");
            showThoughts("Теплая вода - лучший момент за все это время...\nНе считая поцелуя с Кариной...");
        });
        steps.add(() -> {
            setCharacter("miron_thinking.png");
            showThoughts("Поцелуй с Кариной...\nПоцелуй с Ка-ри-ной...\nПоце... ");
        });
        steps.add(() -> {
            setCharacter("miron_shocked.png");
            showThoughts("Вот черт! Я уронил мыло!");
        });
        steps.add(() -> {
            setBackground("mil.jpg");
            List<ChoiceOption> choices = List.of(
                    new ChoiceOption("А. Поднять мыло", () -> handleShowerChoice(1)),
                    new ChoiceOption("Б. Не поднимать", () -> handleShowerChoice(2))
            );
            showChoices(choices);
        });
        startScene(steps);
    }

    private void handleShowerChoice(int choice) {
        List<Runnable> steps = new ArrayList<>();
        if (choice == 1) {
            steps.add(() -> {
                setBackground("vatrushka.jpg");
                showDialogue("Повитухов", "(Вы быстро поднимаете мыло.\nРядом - Повитухов)");
            });
            steps.add(() -> {

                showDialogue("Никита", "Ничего, случается...");
            });
        } else {
            steps.add(() -> {
                setCharacter("miron_scared.png");
                showDialogue("Повитухов", "(Вы решаете не рисковать.\nПовитухов усмехается)");
            });
            steps.add(() -> {

                showDialogue("Никита", "Ничего, случается...");
            });
        }
        steps.add(() -> {
            setBackground("zhes.jpg");
            showThoughts("Нервно взглотнул...");
        });
        steps.add(() -> {

            showDialogue("Мирон", "Помнишь, я хотел поговорить с тобой.");
        });
        steps.add(this::povituhovTalkScene);
        startScene(steps);
    }

    // --- СЦЕНА 6: Разговор с Повитуховым ---
    private void povituhovTalkScene() {
        List<Runnable> steps = new ArrayList<>();
        steps.add(() -> {
            setBackground("vatrushka.jpg");

            showDialogue("Никита", "Помню. Что хотел?");
        });
        steps.add(() -> {

            showDialogue("Мирон", "Ты предлагал облегчить мне отсидку.\nЧто для этого нужно?");
        });
        steps.add(() -> {

            showDialogue("Никита", "Не каждый соглашается...\nНо можно стать ментовским и сдавать всех начальству.\nТогда смягчат срок. Согласен?");
        });
        steps.add(() -> {
            setBackground("zhes.jpg");
            List<ChoiceOption> choices = List.of(
                    new ChoiceOption("А. Согласен", () -> handlePovituhovChoice(1)),
                    new ChoiceOption("Б. Не согласен", () -> handlePovituhovChoice(2))
            );
            showChoices(choices);
        });
        startScene(steps);
    }

    private void handlePovituhovChoice(int choice) {
        List<Runnable> steps = new ArrayList<>();
        if (choice == 1) {
            isRat = true;
            steps.add(() -> {
                setBackground("zloynik.jpg");
                showDialogue("Никита", "Ну ты и крыса!\nЯ тебя проверял на мужика - не прошел!");
            });
        } else {
            isRat = false;
            steps.add(() -> {
                setBackground("dobr.jpg");
                showDialogue("Никита", "Красава! Я знал, что на тебя можно положиться.\nЭто была проверка - ты ее прошел.");
            });
        }
        steps.add(this::tattooScene);
        startScene(steps);
    }

    // --- СЦЕНА 7: Тату-салон ---
    private void tattooScene() {
        List<Runnable> steps = new ArrayList<>();
        steps.add(() -> {
            setBackground("tatus.jpg");
            showDialogue("Татуировщик", "Выбирайте рисунок:");
        });
        steps.add(() -> {

        });

        steps.add(() -> {
            List<ChoiceOption> choices = List.of(
                    new ChoiceOption("А. КУПОЛА", () -> handleTattooChoice(1)),
                    new ChoiceOption("Б. КАРИНА", () -> handleTattooChoice(2)),
                    new ChoiceOption("В. КРЕСТЫ", () -> handleTattooChoice(3))
            );
            showChoices(choices);
        });

        startScene(steps);
    }

    private void handleTattooChoice(int choice) {
        List<Runnable> steps = new ArrayList<>();
        steps.add(() -> {
            setBackground("tatus.jpg");
            if (choice == 1) {
                showDialogue("Татуировщик", "Купола? Ты чё, блатной?");
            } else if (choice == 2) {
                showDialogue("Татуировщик", "Карина? Хм... Личная драма?");
            } else {
                showDialogue("Татуировщик", "Кресты — путь к вере или к боли?");
            }
        });
        steps.add(() -> {
            setBackground("kolshik.jpg");
            showDialogue("Татуировщик", "Фу, такое дерьмо бить тебе не будем!\nНедостоин!");
        });
        steps.add(() -> {
            setBackground("tata.jpg");
            showDialogue("Татуировщик", "(Вам набивают «ЛОХ» на руку.)");
        });
        steps.add(() -> {
            setBackground("duma.jpg");
            showThoughts("Пора разработать план побега!");
        });
        steps.add(() -> {
            escapePlanScene(); // ← Просто запускаем сцену
        });

        startScene(steps);
    }
    // --- СЦЕНА 8: План побега ---
    private void escapePlanScene() {
        List<Runnable> steps = new ArrayList<>();
        steps.add(() -> {
            setBackground("duma.jpg");
            showDialogue("Мирон", "(Варианты побега:)");
        });
        steps.add(() -> {
            // Здесь уже показываются кнопки выбора
            List<ChoiceOption> choices = new ArrayList<>();

            showChoices(choices);
        });


        // ⚠️ Сюда НЕ нужно добавлять nextStep — выбор останавливает цепочку
        steps.add(() -> {
            List<ChoiceOption> choices = new ArrayList<>();
            choices.add(new ChoiceOption("А. Спрятаться в мусорном баке", () -> {
                handleEscapeChoice(1);
            }));

            int optionIndex = 2;
            if (hasSpoon) {
                choices.add(new ChoiceOption("Б. Подкоп ложкой", () -> {
                    handleEscapeChoice(2);
                }));
                optionIndex++;
            }
            if (karinaLikesYou) {
                String optionLetter = (optionIndex == 2) ? "Б" : "В";
                int choiceValue = optionIndex;
                choices.add(new ChoiceOption(optionLetter + ". Попросить Карину помочь", () -> {
                    handleEscapeChoice(choiceValue);
                }));
            }

            showChoices(choices); // ⬅️ Эта строка останавливает дальнейшие шаги
            // НЕ вызывать runNextStep() здесь!
        });

        startScene(steps);
    }



    private void handleEscapeChoice(int choiceValueBasedOnAvailability) {
        List<Runnable> steps = new ArrayList<>();
        String escapeMethodKey = "";

        if (choiceValueBasedOnAvailability == 1) {
            escapeMethodKey = "TRASH";
        } else {
            if (hasSpoon && !karinaLikesYou && choiceValueBasedOnAvailability == 2) escapeMethodKey = "SPOON";
            if (!hasSpoon && karinaLikesYou && choiceValueBasedOnAvailability == 2) escapeMethodKey = "KARINA";
            if (hasSpoon && karinaLikesYou) {
                if (choiceValueBasedOnAvailability == 2) escapeMethodKey = "SPOON";
                if (choiceValueBasedOnAvailability == 3) escapeMethodKey = "KARINA";
            }
        }

        switch (escapeMethodKey) {
            case "TRASH":
                steps.add(() -> {
                    setBackground("bak.jpg");
                    showDialogue("Мирон", "(Вы прячетесь в баке.\nУтром вас вывозят на свалку...)");
                });
                steps.add(() -> {
                    setBackground("mops.jpg");
                    showDialogue("Мирон", "(Вы свободны!\nНо внезапно нападают дикие псы...)");
                });
                steps.add(() -> showEnding("Собачья смерть"));
                break;

            case "SPOON":
                steps.add(() -> {
                    setBackground("podkop.jpg");
                    showDialogue("Мирон", "(3 месяца кропотливого труда...\nПодкоп готов!)");
                });
                steps.add(() -> {
                    setBackground("pobeg.jpg");
                    showDialogue("Мирон", "(Вы свободны!\nНо впереди долгий путь...)");
                });
                steps.add(() -> showEnding("Классический побег"));
                break;

            case "KARINA":
                steps.add(() -> {
                    setCharacter("STOMATCHOX.jpg");
                    showDialogue("Мирон", "(Карина прячет вас у себя в желудке/помогает другим способом...\nЧерез неделю вы на свободе!)");
                });
                steps.add(() -> showEnding("Любовь спасет мир"));
                break;

            default:
                steps.add(() -> showDialogue("Система", "Произошла ошибка в выборе пути побега."));
                break;
        }
        startScene(steps);
    }

    // --- КОНЦОВКА ---
    // --- КОНЦОВКА ---
    private void showEnding(String endingTitle) {
        setBackground("tralale.jpg");
        setCharacter(null);
        dialogueBox.setVisible(true);
        choiceBox.setVisible(false);
        speakerLabel.setText("КОНЕЦ");
        mainText.setText(endingTitle);
        mainText.setTextAlignment(TextAlignment.CENTER);

        currentSceneSteps = null;
        currentStepIndex = 0;
        dialogueBox.setOnMouseClicked(null);

        // ⬇ Создаем жирную мигающую кнопку
        Button leaveButton = new Button("ЛИВАЙ ИЗ МИРА");
        leaveButton.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color: red; -fx-text-fill: white;");
        leaveButton.setOnAction(e -> {
            System.out.println("Игрок ливнул из мира.");
            Platform.exit(); // Закрыть приложение
        });

        // ⬇ Анимация мигания кнопки
        FadeTransition blink = new FadeTransition(Duration.seconds(0.5), leaveButton);
        blink.setFromValue(1.0);
        blink.setToValue(0.3);
        blink.setCycleCount(Animation.INDEFINITE);
        blink.setAutoReverse(true);
        blink.play();

        // ⬇ Добавляем кнопку на основной экран (root или твою панель)
        // ⬇ Добавляем кнопку на основной экран
        rootPane.getChildren().add(leaveButton);
        StackPane.setAlignment(leaveButton, Pos.BOTTOM_CENTER); // Позиция по центру снизу
        StackPane.setMargin(leaveButton, new Insets(20));

    }

}

