package com.example.SpringBot.service;

import com.example.SpringBot.config.BotConfig;
import com.example.SpringBot.model.Month;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {


    @Autowired
    private final SalaryServices salaryServices;
    @Autowired
    private final JokeServices jokeServices;
    final BotConfig config;
    final String YES = "YES_BUTTON";
    final String NO = "NO_BUTTON";
    static final String HELP_TEXT = "Мои команды\n1. Посчитать зп за сегодня(ввседи команду salary и выручку, через пробел) Например: salary 150300\n" +
            "2. Добавить зп вручную: +зп 8500 отпуск (команда, сумма денег, коментарий если нужен)\n" +
            "3. Посмотреть зп за месяц: month 05 (команда, месяц) \n" +
            "4. Посмотреть сумму за за месяц: sum 11 (команда, месяц)";

    @Autowired
    public TelegramBot(SalaryServices salaryServices, JokeServices jokeServices, BotConfig config) {
        this.salaryServices = salaryServices;
        this.jokeServices = jokeServices;
        this.config = config;
        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", "Начало работы"));
        listOfCommand.add(new BotCommand("/last", "Покажет зп за прошлую смену"));
        listOfCommand.add(new BotCommand("/joke", "Хочешь шутку?"));
        listOfCommand.add(new BotCommand("/all", "Показывает всю зп по дням"));

        listOfCommand.add(new BotCommand("/help", "Список комнад"));
//        listOfCommand.add(new BotCommand("/toDo", "Тут ещё будут команды"));
        try {
            this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String msgText = update.getMessage().getText();
            String[] msgParse = msgText.split(" ");
            String message = msgParse[0];
            String revenue = "";
            String data = "";
            switch (msgParse.length) {
                case 2:
                    revenue = msgParse[1];
                    break;
                case 3:
                    revenue = msgParse[1];
                    data = msgParse[2];
                    break;
            }
            long chatId = update.getMessage().getChatId();

            switch (message.toLowerCase()) {
                case "/start":
                    startCommand(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "salary":
                    salarySave(chatId, revenue, update.getMessage());
                    break;
                case "s":
                    temporaryCommand(chatId, revenue, data);
                    break;
                case "+зп":
                    salaryServices.addMoney(revenue, data, update.getMessage());
                    sendMessage(chatId, "Всё ОК)");
                    break;
                case "/joke":
                    sendMessage(chatId, jokeServices.getJoke());
                    break;
                case "/all":
                    getByAllSalary(chatId, update.getMessage().getChat().getUserName());
                    break;
                case "/last":
                    lastSalary(chatId, update.getMessage().getChat().getUserName());
                    break;
                case "хочешь_шутку?":
                    proof(chatId);
                    break;
//                case "моя_зарплата":
//                    actionSelection(chatId);
//                    break;
                case "month":
                    getSalaryInAMonth(chatId, update.getMessage().getChat().getUserName(), revenue);
                    break;
                case "sum":
                    sumSalaryInAMonth(chatId, update.getMessage().getChat().getUserName(), revenue);
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Я не знаю такой команды");
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String text = "";

            switch (callbackData) {
                case YES:
                    text = jokeServices.getJoke();
                    break;
                case NO:
                    text = "Ну как хочешь O_o";
                    break;
                case "mount":
                    text = "зп за месяц";
                    monthSelection(chatId);
//                    sumSalaryInAMonth(chatId, update.getMessage().getChat().getUserName());
                    break;
                case "day":
                    text = lastSalary(update.getMessage().getChat().getUserName());
                    break;
                case "all":
                    getByAllSalary(chatId, update.getMessage().getChat().getUserName());
                    break;
            }
            EditMessageText msg = new EditMessageText();
            msg.setChatId(chatId);
            msg.setText(text);
            msg.setMessageId(messageId);

            try {
                execute(msg);
            } catch (TelegramApiException e) {
                log.error("Error setting bot's command list: " + e.getMessage());
            }
        }


    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Хочешь_шутку?");

//        keyboardRows.add(row);
//        row = new KeyboardRow();
//        row.add("моя_зарплата");

        keyboardRows.add(row);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    private void startCommand(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Привет, " + name + " :blush:");
        log.info("Replace to user" + name);
        sendMessage(chatId, answer);
    }

    private void salarySave(long chatId, String revenue, Message msg) {
        String name = msg.getChat().getUserName();
        int salary = salaryServices.calculateSalary(revenue, name);
        String textSend;
        textSend = String.valueOf(salary);
        sendMessage(chatId, textSend);
    }


    private void proof(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Точно хочешь шутку, тебя предупреждали?");

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData(YES);

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData(NO);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        inlineKeyboard.setKeyboard(rowsInLine);
        message.setReplyMarkup(inlineKeyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }

    }

    private void actionSelection(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Я могу показать всю зарплату за месяц, сумму за выбраный месяц и за день");

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton mountButton = new InlineKeyboardButton();
        mountButton.setText("Месяц");
        mountButton.setCallbackData("month");

        InlineKeyboardButton sumButton = new InlineKeyboardButton();
        sumButton.setText("Вся зп");
        sumButton.setCallbackData("all");

        InlineKeyboardButton dayButton = new InlineKeyboardButton();
        dayButton.setText("День");
        dayButton.setCallbackData("day");

        rowInLine.add(mountButton);
        rowInLine.add(sumButton);
        rowInLine.add(dayButton);

        rowsInLine.add(rowInLine);

        inlineKeyboard.setKeyboard(rowsInLine);
        message.setReplyMarkup(inlineKeyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }

    }

    /**
     * Метод отрисовки инлайн клавиатуры с выбором месяца
     *
     * @param chatId
     */

    private void monthSelection(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(String.valueOf(LocalDate.now().getYear()));

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton button;

        int count = 1;
        for (Month allMounts : Month.values()) {
            button = new InlineKeyboardButton();
            button.setText(allMounts.getTitle());
            String mount = String.valueOf(allMounts);
            button.setCallbackData(mount);

            rowInLine.add(button);
            if (count % 3 == 0) {
                rowsInLine.add(rowInLine);
                rowInLine = new ArrayList<>();
            }
            count++;
        }

        inlineKeyboard.setKeyboard(rowsInLine);
        message.setReplyMarkup(inlineKeyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }

    }

    /**
     * Метод сохранения дохода, на прямую в бд с ручным вводом коментария
     *
     * @param chatId
     * @param name
     */

    public void getByAllSalary(long chatId, String name) {
        String textSend = salaryServices.getAllSalary(name);
//        String textSend = salaryServices.getAllSalary("Ray");
        sendMessage(chatId, textSend);
    }

    /**
     * Метод ищет последнюю записанную зп пользователя с name
     *
     * @param chatId
     * @param name   - имя пользователя
     */

    private void lastSalary(long chatId, String name) {
        String textSend = salaryServices.getLastSalary(name);
//        String textSend = salaryServices.getAllSalary("Ray");
        sendMessage(chatId, textSend);
    }

    private String lastSalary(String name) {
        String textSend = salaryServices.getLastSalary(name);
//        String textSend = salaryServices.getAllSalary("Ray");
        return textSend;
    }

    private void getSalaryInAMonth(long chatId, String name, String data) {
        int month = Integer.valueOf(data);
        LocalDate startDay = LocalDate.of(LocalDate.now().getYear(), month, 1);
        LocalDate endDay = (LocalDate) TemporalAdjusters.lastDayOfMonth().adjustInto(startDay);
        String textSend = salaryServices.getSalaryInAMonth(name, startDay, endDay);
        sendMessage(chatId, textSend);
    }

    private void sumSalaryInAMonth(long chatId, String name, String data) {
        int month = Integer.valueOf(data);
        LocalDate startDay = LocalDate.of(LocalDate.now().getYear(), month, 1);
        LocalDate endDay = (LocalDate) TemporalAdjusters.lastDayOfMonth().adjustInto(startDay);
        int sum = salaryServices.getSumSalaryInAMonth(name, startDay, endDay);
        String textSend = String.valueOf(sum);
        sendMessage(chatId, textSend);
        System.out.println(startDay);
        System.out.println(endDay);
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }


    /**
     * Временный метод для сохранения зп нескольких человек за один запуск бота
     * @param chatId
     * @param revenue
     */


    private void temporaryCommand(long chatId, String revenue, String name) {
        String userName = "";

        if(name.equals("1")){
            userName = "Ray";
        } else {
            userName = "Nataxa";
        }
        int salary = salaryServices.calculateSalary(revenue, userName);
        String textSend = userName + " - " + salary;
        sendMessage(chatId, textSend);
    }
}
