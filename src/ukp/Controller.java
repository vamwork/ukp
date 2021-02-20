package ukp;

import com.cinterion.io.ATCommand;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class Controller {
    // x - изменения для всех версий
    // d - TC65, Меркурий230
    // s - TC65, СЭТ-4ТМ
    // e - smart Меркурий230
    // n - smart, СЭТ-4ТМ
    // z - IRZ TU41 (EHS5)

    // X.XXt признак тестовой версии, после проведения тестирования t заменяется на d
    // 3.39d последняя рабочая версия без управления
    // 4.01d первая версия с управлением
    // 4.04d добавлен перезапуск в методе destroyApp, управление предварительно - Ok
    // 4.05d "двойной" пинг
    // 4.06d доделки для любого количества дискр. каналов, в том числе длина  записи дис. журнала стала =8       
    // 4.06d передана в производство 27.11.07
    // 4.08d исправлена мелкая ошибка (key=0 при добавлении ОУ при отсутввии каналов) передана в производство 28.11.07
    // 4.09d без изменений 29.11.07
    // 4.10d добавлены команды тестирования выходных реле
    // 4.11d исправлена критическая ошибка дискретных каналов
    // 4.12d добавлен перезапуск контроллера 17.01.08
    // 4.13d перезапуск контроллера в 0:28
    // 4.14d в destroyApp удалена перезагрузка модема - иначе с OTAP не работает
    // 4.15d relese candidate 6.02.08
    // 4.16d небольшие изменения в перезапуске (07.09.08)
    // 4.17d исправлена ошибка в EqaualsMessage - получасовки сравниваются с минутами, заблоктрван перезапуск контроллера [28.08.08]
    // 4.18d перезапуск контроллера вынесен в класс Midlet [06.11.08]
    // 4.19d добавлено чтение параметра Энергия за предыдщие сутки [20.11.08]
    // 4.21d работа с расписаниями + Энергия за пред месяц [24.11.08]
    // 4.22d измененено количество сообщений в пакете [05.12.08]
    // 4.23d промежуточная версия без изменений [14.12.08]
    // 4.24d при отработке цикла управления делает 3 попытки чтения IN [22.12.08]
    // 4.25d создает папку storage при запуске
    // 4.26d при чтении состяния DIN добавлена задержка м/у попытками [25.12.08]
    // 4.27d возвращет unknown если desHost и connectionProfile не заданы [25.12.08]
    // 4.28d не было
    // 4.29d исправлена ошибка попыток опроса DIN [26.12.08]
    // 4.30d доработан метод Command - добавлено открытие канала при необходимости, добавлен опрос по расписанию энергии на начало суток [10.01.09]
    // 4.31d промежуточная версия. Исправлены ошибки сбора параметров №2 и №4. Расписания без проверки [18.01.09]
    // 4.32d промежуточная отладочная версия [20.01.09]
    // 4.33d промежуточная. С данными "энергия по расписанию" добавлен Kсч  [20.01.09]
    // 4.34d промежуточная. [12.02.09]
    // 4.35t тестовая с частым сбором профилей тока и напряжения
    // 4.36х разделение версий для Меркурия230 и СЭТ-4ТМ [11.04.09]
    // 4.37x модернизирована процедура создания папки storage [15.04.09]
    // 4.38d модернизирована процедура отправки SMS
    // 4.39t watchdog [28.06.09]
    // 4.40t relaease candidate. WatchDog, cбор суточных данных после 1.00 [02.07.09]
    // 4.40d отдана в производство. WachDog. Нет отправки sms [04.07.09]
    // 4.41d работа с балансом на карте [19.07.09]
    // 4.42d прошивка для ПЭС [21.07.09]
    // 4.43 промежуточная версия [23.09.09]
    // 4.45e первая версия для smart со сторожевым таймером и вх.дискр [30.08.09]
    // 4.46e в производство [29.10.09]    
    // 4.48e release candidate. добавлен параметр 6 - энергия по расписанию по
    // тарифам [06.02.10]
    // 4.49e release candidate. Добавлена работа с дискретными модулями [16.02.10]
    // 4.53e в производство [16.02.10]
    // 4.54e в производство увеличено время "сброса" WDT [09.03.10]
    // 4.55e попытка исправить чтение профилей с меркуриев со сдвигом метки времени [24.03.10]
    // 4.56e попытка исправить чтение профилей для счетчиков у котороых время на 1 час вперед. [26.03.10]
    // 4.57e перед опросом зафиксированной энергии добавлено открытие канала счётчика [26.03.10]
    // 4.59 доработан опрос профилей для обхода "ошибок" меркурия [03.03.10]
    // 4.60 исправлены ошибки определения Ксч [10.04.10]
    // 4.64 работа с событиями [30.05.10]
    // 4.66 пробная версия. Работа с событиями контроллера [19.12.10]
    // 4.67 пробная версия. Работа с событиями контроллера [05.01.11]
    // 4.68 Release candidate. Работа с событиями [07.01.11]
    // 4.69 Release candidate. "Умный" перезапуск [13.04.11]
    // 5.02 Небольшие исправления. Release Candidate [06.06.11]
    // 5.03 Работа с диском - 0xE9 (available) и 0xE0 (1-clean2-clear)[20.10.11]
    // 5.04 В производство [13.11.11]
    // 5.12 Техническое повышение и синхронизация TC65&Smart [03.06.12] (smart - master версия)
    // 5.14 Поддержка счетчиков меркурий 233 [10.02.13]
    // 5.15 Переподключения к сети, глубина архива 32 суток [10.02.13]
    // 5.16 Работа со всроенными GPIO [14.09.13]
    // 6.01 Добавлены Новотест PLC [13.12.14]
    // 6.10 Управление GPIO5 [10.10.15]
    // 7.06 z с датчиком мощности [19.03.17]
    // 7.07 z исправлены ощибки опрса Меркурий230-234 [25.06.17]
    // 7.11 в производство [27.06.17]
    // 7.12 в производство [23.06.17]
    // 7.14 в производство [01.09.17]
    // 7.15 Led-индикация уровня сигнала и связи с сервером [02.09.17]
    // 7.16 Меркурий 23х и Меркурий 20х в одной прошивке [09.09.17]
    // 7.17 Управление 206 промежуточная [16.09.17]
    // 7.18 предварительная [19.09.17]
    // 7.19 предварительная [23.09.17]
    // 7.20 Производство [23.09.17]
    // 7.23 Эксперименты с reboot [30.09.17]
    // 7.25 Производство [01.10.17]
    // 7.26 Техническое повышение [01.10.17]
    // 7.40 Мониторинг линии, sms рассылки
    // 7.42 Контроль положения двери [16.09.18]
    // 7.47 Начато исправление циклического чтения конфигурации [02.10.2018]
    // 7.48 Оптимизированы методы работы со счетчиками (адрес 0 нельзя и т.д.) [06.10.2018]
    // 7.49 Техническое повышение [27.03.2019]
    // 7.50 Доделана команда очистки диска E0,02 с последующим перезапуском [31.03.2019]
    // 7.51 Промежуточная в эксплуатацию [06.05.2019]
    // 7.54 Уведичен период срабатывания WD [12.05.2019]
    // 8.01 Версия под новые EHS5
    // 8.03 Версия под новые EHS5 [29.11.2020]
    // 8.04 Техническое повышение [08.01.2021]
    // 8.05 Отключена запись архивов 5 контроллеров Приозерск[08.01.2021] 
    
    public static final byte[] version = {'8', '.', '0', '6', 'z'};

    public static int rebootCounter = 0;   //счетчик для WD
    
    //public static final String controllerName = "UKP1 ";
    public static byte[] autoParameters = new byte[16]; // массив признаков сборов параметров

    public static Vector shedule = new Vector();

    private static final int openChannelMs = 30000; // 30сек время удерживания канала
    
    public static int channelIsOpen = 0; // счётчик открытого канала

    public static boolean timeIsSynchro = false; // признак проведенной синхронизации

    public static String currentBalans = "";

    public static String timeStampOfBalans = "";

    public static boolean toSendOfBalans = false;

    public static long timeCorrector = 0; // корректор времени

    private static final String configFileName = "cntrl.cnf";

    private static final String phonesFileName = "phones.cnf";

    private static final String sheduleFileName = "shedule.cnf";

    // АТ-команда на выполнение
    public static String atCommandToExec = "";

    // -------------------------------------------------------------------------
    /*
	 * Список задействованных команд 0 1 3 4 21 22 23 24 25 A0 A1 A2 A3 A4 A5 A6
	 * A7 A8 A9 AA B0 B1 B2 B4 B5 B6 B7 B8 B9 BA BB BC BD C1 C2 C3 C6 C7 C8 CA E0 
         * E1 E2 E3 E4 E5 E6 E8 E9 EA EB EC ED D0 D1 D2 D3 D4 D5 D6 D7 D8 D9 DA DB DC 
         * DD DE CC F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FB FC FF 
         * 51,52,53
     */
    // Контроллер
    private static final byte PING = (byte) 0x0; // realize
    private static final byte GET_TIME = (byte) 0x4; // realize
    private static final byte GET_VERSION = (byte) 0xB6; // realize
    private static final byte OPEN_CHANNEL = (byte) 0x1; // realize
    private static final byte CHANGE_PASSWORD = (byte) 0x3; // realize
    private static final byte TIME_SYNCHRO = (byte) 0xA2; // realize
    private static final byte SET_PHONE = (byte) 0xB1; // realize
    private static final byte GET_PHONE = (byte) 0xBB; // realize
    private static final byte SET_NET_ADDR = (byte) 0xB5; // realize
    private static final byte GET_EXT_NET_ADDR = (byte) 0xB2; // realize
    private static final byte GET_DIALING_RULE = (byte) 0xEC; // realize
    private static final byte SET_DIALING_RULE = (byte) 0xED; // realize
    private static final byte SET_DEST_HOST = (byte) 0xD0; // realize
    private static final byte GET_DEST_HOST = (byte) 0xD1; // realize
    private static final byte SET_CONN_PROFILE = (byte) 0xD2; // realize
    private static final byte GET_CONN_PROFILE = (byte) 0xD3; // realize
    private static final byte SET_NET_PASS = (byte) 0xD4; // realize
    private static final byte GET_NET_PASS = (byte) 0xD5; // realize
    private static final byte SET_NET_INTERVAL = (byte) 0xD6; // realize
    private static final byte GET_NET_INTERVAL = (byte) 0xD7; // realize
    private static final byte SET_USE_SSL = (byte) 0xD8; // realize
    private static final byte GET_USE_SSL = (byte) 0xD9; // realize
    private static final byte REMOVE_ALL_FILES = (byte) 0xFF;

    // АТ-команды
    private static final byte GET_AT_COMMANDS_COUNT = (byte) 0x21; // realize
    private static final byte GET_AT_COMMAND = (byte) 0x22; // realize
    private static final byte DELETE_AT_COMMAND = (byte) 0x23; // realize
    private static final byte INSERT_AT_COMMAND = (byte) 0x24; // realize
    private static final byte AT_COMMANDS_TO_DISC = (byte) 0x25; // realize
    private static final byte EXECUTE_ONE_COMMAND = (byte) 0x26; // выполнить одну команду

    // Счётчики
    private static final byte MERC_COUNT = (byte) 0xA0; // realize
    private static final byte MERC_LIST = (byte) 0xA1; // realize
    private static final byte MERC_MASTER = (byte) 0xA3; // realize
    private static final byte ADD_MERC = (byte) 0xE1; // realize
    private static final byte DELETE_MERC = (byte) 0xE2; // realize
    private static final byte SET_MERC_PASSWORD = (byte) 0xB4; // realize

    // Датчики мощности    
    private static final byte PS_LIST = (byte) 0xC6;
    private static final byte PS_ADD = (byte) 0xC7;
    private static final byte PS_REMOVE = (byte) 0xC8;

    // Профиль
    private static final byte GET_PROFILE_INFILL = (byte) 0xBC; // realize
    private static final byte OPEN_PROFILE = (byte) 0xA4; // realize
    private static final byte GET_PROFILE_RECORD = (byte) 0xA5; // realize

    // Энергия
    private static final byte GET_ENERGY_INFILL = (byte) 0xC1; // realize
    private static final byte OPEN_ENERGY = (byte) 0xC2; // realize
    private static final byte GET_ENERGY_RECORD = (byte) 0xC3; // realize

    // Аналоговые каналы и аналоговы журнал
    private static final byte GET_CURRENT_ANALOG = (byte) 0xE3; // realize
    private static final byte GET_EVENTS_COUNT = (byte) 0xA6; // realize
    private static final byte GET_EVENT = (byte) 0xA7; // realize
    private static final byte SET_MAX = (byte) 0xE4; // realize
    private static final byte SET_MIN = (byte) 0xE5; // realize
    private static final byte SET_ZERRO = (byte) 0xE6; // realize
    private static final byte GET_CONDITIONS = (byte) 0xE8; // realize
    private static final byte CLEAR_EVENTS = (byte) 0xAA; // realize

    // Дискретные каналы и дискретный журнал
    private static final byte DISCRET_OUT = (byte) 0xA8; // realize
    private static final byte GET_DISCRET_IN = (byte) 0xA9; // realize
    private static final byte SET_DISCRET_CONDITIONS = (byte) 0xB0; // realize
    private static final byte GET_DISCRET_CONDITIONS = (byte) 0xB7; // realize
    private static final byte CLEAR_DISCRET_EVENTS = (byte) 0xB8; // realize
    private static final byte GET_DISCRET_EVENTS_COUNT = (byte) 0xB9; // realize
    private static final byte GET_DISCRET_EVENT = (byte) 0xBA; // realize
    private static final byte GET_IN_OUT_COUNT = (byte) 0xBD;

    // Управление
    private static final byte CONTROL_COMMAND = (byte) 0xCC; // realize

    // Флаг новых событий
    private static final byte GET_NEW_EVENTS_FLAG = (byte) 0xEA;
    private static final byte CLEAR_NEW_EVENTS_FLAG = (byte) 0xEB;

    // Работа с расписаниями
    private static final byte GET_AUTO_PARAMETERS = (byte) 0xDA;
    private static final byte SET_AUTO_PARAMETERS = (byte) 0xDB;
    private static final byte GET_SHEDULE = (byte) 0xDC;
    private static final byte SET_SHEDULE = (byte) 0xDD;

    // группа команд для определения баланса счета
    private static final byte SEND_BALANCE = (byte) 0xDE;

    // работа с памятью
    private static final byte GET_AVAILABLE = (byte) 0xE9;
    private static final byte CLEANING = (byte) 0xE0;

    // Новотест PLC
    private static final byte GET_NOVO_PLC_LIST = (byte) 0xF1;
    private static final byte ADD_NOVO_PLC = (byte) 0xF2;
    private static final byte REMOVE_NOVO_PLC = (byte) 0xF3;
    private static final byte ADD_MERC_TO_NOVO_PLC = (byte) 0xF4;
    private static final byte REMOVE_MERC_FROM_NOVO_PLC = (byte) 0xF5;
    private static final byte GET_MERC_BY_NOVO_PLC = (byte) 0xF6;
    private static final byte SET_NOVO_PLC_MASTER_MERC = (byte) 0xF7;
    private static final byte GET_NOVO_PLC_MASTER_MERC = (byte) 0xF8;
    private static final byte NOVO_PLC_MERC_PING = (byte) 0xF9;

    //GPIO5
    private static final byte POWER_GPIO5 = (byte) 0xCA;

    //Меркурий 20x
    private static final byte ADD_MERC200 = (byte) 0xAB;
    private static final byte DEL_MERC200 = (byte) 0xAC;
    private static final byte MERC200_CNT = (byte) 0xAD;
    private static final byte MERC200_INFO = (byte) 0xAE;

    //Управление нагрузкой
    private static final byte POWER_ON = (byte) 0xFA;
    private static final byte POWER_OFF = (byte) 0xFB;
    private static final byte POWER_STATE = (byte) 0xFC;

    //Список телефонов рассылки
    private static final byte GET_PHONES = (byte) 0x51;
    private static final byte ADD_PHONE = (byte) 0x52;
    private static final byte REMOVE_PHONE = (byte) 0x53;
    private static final byte REMOVE_ALL_PHONES = (byte) 0x54;
    // -------------------------------------------------------------------------
    public static Runtime runtime = Runtime.getRuntime();
    public static byte netAddr = 0;
    public static byte extendedNetAddr = 0; // расширенный сетевой адрес
    public static ComPort cp = new ComPort();

    // public static ComPort cp232 = new ComPort(0);
    public static ATCommand atc;
    public static byte[] password = {1, 1, 1, 1, 1, 1}; // defaul password
    public static final byte[] sprVsrPass = {'s', 'p', 'r', 'v', 'S', 'r'}; // supervisor-пароль
    public static final byte[] masterPass = {'m', 'A', 's', 't', 'e', 'r'}; // мастер-пароль  6d 41 73 74 65 72
    public static int currentMode = 0; // режим 0-обычный режим 1-мастер-режим
    public static byte[] phone = new byte[11]; // телефонный номер ДП
    public static byte[] disConditions = new byte[256]; // условия фиксации дискретных сигналов
    public static byte toDialing = 0; // правила дозвона в ДП
    public static byte[] simCid = new byte[20]; // номер sim-карты
    public static byte[] serialNumber = new byte[15]; // serial number of device   
    public static String destHost = "";
    public static String connectionProfile = "";
    public static boolean useSSL = false;
    public static String netPassword = "default";
    public static int webInterval = 30;
    public static boolean isLocalMode;

// установить счётчик открытости канала
    public static void SetChannelIsOpen() {
        channelIsOpen = openChannelMs;
    }

    // разборщик команд
    public static byte[] Parse(byte[] command) {
        byte[] answer = {0x1};
        switch (command[1]) {
            case PING:
                answer = Ping();
                break;
            case OPEN_CHANNEL:
                answer = OpenChannel(command);
                break;
            case CHANGE_PASSWORD:
                answer = ChangePassword(command);
                break;
            case GET_TIME:
                answer = GetTime();
                break;
            case GET_VERSION:
                answer = GetVersion();
                break;
            case TIME_SYNCHRO:
                answer = TimeSynchro();
                break;
            case MERC_MASTER:
                answer = GetMasterAddr();
                break;
            case GET_PHONE:
                answer = GetPhone();
                break;
            case MERC_COUNT:
                answer = GetMercCount();
                break;
            case SET_PHONE:
                answer = SetPhone(command);
                break;
            case SET_NET_ADDR:
                answer = SetNetAddr(command);
                break;
            case GET_EXT_NET_ADDR:
                answer = GetExtNetAddr(command);
                break;
            case MERC_LIST:
                answer = GetMercList();
                break;
            case ADD_MERC:
                answer = AddMerc(command);
                break;
            case DELETE_MERC:
                answer = DeleteMercury(command);
                break;
            case SET_MERC_PASSWORD:
                answer = SetMercPassword(command);
                break;
            case GET_DIALING_RULE:
                answer = GetDialingRule();
                break;
            case SET_DIALING_RULE:
                answer = SetDialingRule(command);
                break;
            case GET_CURRENT_ANALOG:
                answer = GetCurrentAnalog(command);
                break;
            case GET_EVENTS_COUNT:
                answer = GetEventsCount();
                break;
            case GET_EVENT:
                answer = GetEvent(command);
                break;
            case CLEAR_EVENTS:
                answer = ClearEvents();
                break;
            case SET_MAX:
                answer = SetValueConditions(command);
                break; // !!! One procedure!
            case SET_MIN:
                answer = SetValueConditions(command);
                break; // !!! One procedure!
            case SET_ZERRO:
                answer = SetZerroConditions(command);
                break;
            case GET_CONDITIONS:
                answer = GetConditions(command);
                break;
            case OPEN_PROFILE:
                answer = OpenProfile(command);
                break;
            case GET_PROFILE_INFILL:
                answer = GetProfileInfill();
                break;
            case GET_PROFILE_RECORD:
                answer = GetProfileRecord(command);
                break;           
            case SET_DISCRET_CONDITIONS:
                answer = SetDiscretConditions(command);
                break;
            case GET_ENERGY_INFILL:
                answer = GetEnergyInfill();
                break;
            case OPEN_ENERGY:
                answer = OpenEnergy(command);
                break;
            case GET_ENERGY_RECORD:
                answer = GetEnergyRecord(command);
                break;
            case SET_DEST_HOST:
                answer = SetDestHost(command);
                break;
            case GET_DEST_HOST:
                answer = GetDestHost();
                break;
            case SET_CONN_PROFILE:
                answer = SetConnProfile(command);
                break;
            case GET_CONN_PROFILE:
                answer = GetConnProfile();
                break;
            case SET_NET_PASS:
                answer = SetNetPass(command);
                break;
            case GET_NET_PASS:
                answer = GetNetPass();
                break;
            case SET_NET_INTERVAL:
                answer = SetNetInterval(command);
                break;
            case GET_NET_INTERVAL:
                answer = GetNetInterval();
                break;
            case SET_USE_SSL:
                answer = SetUseSSL(command);
                break;
            case GET_USE_SSL:
                answer = GetUseSSL();
                break;            
            case GET_AUTO_PARAMETERS:
                answer = GetAutoParameters();
                break;
            case SET_AUTO_PARAMETERS:
                answer = SetAutoParameters(command);
                break;
            case GET_SHEDULE:
                answer = GetShedule();
                break;
            case SET_SHEDULE:
                answer = SetShedule(command);
                break;
            case SEND_BALANCE:
                answer = SendBalance(command);
                break;
            case EXECUTE_ONE_COMMAND:
                answer = ExecuteOneAtCommand(command);
                break;
            case CLEANING:
                answer = DiskCleaning(command);
                break;
            case POWER_GPIO5:
                answer = PowerGPIO5(command);
                break;
            case REMOVE_ALL_FILES:
                answer = RemoveAllFiles(command);
                break;
            case ADD_MERC200:
                answer = AddMerc200(command);
                break;
            case DEL_MERC200:
                answer = DelMerc200(command);
                break;
            case MERC200_CNT:
                answer = Merc200Cnt();
                break;
            case MERC200_INFO:
                answer = GetMerc200Info(command);
                break;
            case POWER_ON:
                answer = PowerOn(command);
                break;
            case POWER_OFF:
                answer = PowerOff(command);
                break;
            case POWER_STATE:
                answer = PowerState(command);
                break;
            case GET_PHONES:
                answer = GetPhones();
                break;
            case ADD_PHONE:
                answer = AddPhone(command);
                break;
            case REMOVE_PHONE:
                answer = RemovePhone(command);
                break;
            case REMOVE_ALL_PHONES:
                answer = RemoveAllPhones();
                break;

        }
        return Global.MakeCommand(answer, netAddr);
    }

    public static boolean CheckSimAndSrlNumber(byte[] simCid,
            byte[] serialNumber) {
        boolean result = false;
        if (Global.ArrayEquals(simCid, Controller.simCid) & Global.ArrayEquals(serialNumber, Controller.serialNumber)) {
            result = true;
        }
        return result;
    }

    // сохранить конфигурацию
    public static void SaveConfig() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + configFileName);
            if (!fConn.exists()) {
                fConn.create();
            }
            OutputStream os = fConn.openOutputStream(0);
            os.write(netAddr);
            os.write(extendedNetAddr);
            os.write(password);
            os.write(phone);
            os.write(disConditions);
            os.write(toDialing);

            // запишем в поток дополнительные параметры для GPRS
            os.write(Global.GetStringAsArray(destHost));
            os.write(Global.GetStringAsArray(connectionProfile));

            if (useSSL) {
                os.write((byte) 1);
            } else {
                os.write((byte) 0);
            }
            os.write(Global.GetStringAsArray(netPassword));
            os.write((byte) webInterval);
            os.close();
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*Controller[SaveConfig]" + ex.toString());
        }
    }

    // прочитать конфигурацию
    public static void LoadConfig() {
        try {
//            netAddr = 0;
//            extendedNetAddr = 0;
//            password = new byte[]{1, 1, 1, 1, 1, 1};
//            destHost = "silesta.azurewebsites.net/service/TST.ashi";
            //destHost = "91.237.32.117/silesta/service/TST.ashi";
//            connectionProfile = "bearer_type=gprs;access_point=internet.mts.ru;username=mts;password=mts";

            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + configFileName);
            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                is.mark(0);

                if (is.available() != 0) {
                    netAddr = (byte) is.read();
                    extendedNetAddr = (byte) is.read();
                    is.read(password);
                    is.read(phone);
                    is.read(disConditions);
                    toDialing = (byte) is.read();

                    // дополнительные настройки для GPRS
                    destHost = Global.ReadString(is);
                    connectionProfile = Global.ReadString(is);
                    if (is.read() == 1) {
                        useSSL = true;
                    } else {
                        useSSL = false;
                    }
                    netPassword = Global.ReadString(is);

                    webInterval = is.read();
                }
                is.close();
            }
            fConn.close();                                    
        } catch (Exception ex) {
            Logger.ErrToLog("*Controller[LoadConfig]" + ex.toString());
        }
    }

    // прочитать список телефонов рассылки
    public static void LoadPhones() {
        //SmsService.phones.addElement("+79033061297");
        //SmsService.phones.addElement("+79178803473");
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + phonesFileName);
            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                if (is.available() != 0) {
                    byte[] p = new byte[12];
                    is.read(p);
                    String phone = Global.ToString(p);                    
                    SmsService.phones.addElement(phone);
                }
                is.close();
            }
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*Controller[LoadConfig]" + ex.toString());
        }
    }

    // сохраниеть список телефонов
    public static void SavePhones() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + phonesFileName);
            if (!fConn.exists()) {
                fConn.create();
            }
            OutputStream os = fConn.openOutputStream(0);

            for (int i = 0; i < SmsService.phones.size(); i++) {
                String number = (String) SmsService.phones.elementAt(i);
                os.write(Global.StringToBytes(number));
            }
            os.close();
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*Controller[SavePhones]" + ex.toString());
        }
    }

    public static void SaveShedule() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + sheduleFileName);
            if (!fConn.exists()) {
                fConn.create();
            }
            OutputStream os = fConn.openOutputStream(0);

            for (int i = 0; i < autoParameters.length; i++) {
                os.write(autoParameters[i]);
            }
            os.write((byte) shedule.size());
            for (int i = 0; i < shedule.size(); i++) {
                os.write(((Byte) shedule.elementAt(i)).byteValue());
            }
            os.close();
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*Controller[SaveShedule]" + ex.toString());
        }
    }

    public static void LoadShedule() {
        // установим значения по-умолчанию
        for (int i = 0; i < autoParameters.length; i++) {
            autoParameters[i] = 1;
        }
        shedule.removeAllElements();

        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + sheduleFileName);
            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                if (is.available() != 0) {
                    for (int i = 0; i < autoParameters.length; i++) {
                        autoParameters[i] = (byte) is.read();
                    }
                    byte sheduleCount = (byte) is.read();
                    shedule.removeAllElements();
                    for (int i = 0; i < sheduleCount; i++) {
                        byte s = (byte) is.read();
                        shedule.addElement(new Byte(s));
                    }
                }
                is.close();
            } else {
                //файла с расписанием нет, создадим по умолчанию
                shedule.addElement(new Byte((byte) 33));
            }
            fConn.close();

        } catch (Exception ex) {
            Logger.ErrToLog("*Controller[LoadShedule]" + ex.toString());
        }
    }  

    private static byte[] RemoveAllFiles(byte[] command) {
        byte[] answer = {0x6};
        try {
            if (command[2] == (byte) 0xFE) {
                FileConnection fconn = (FileConnection) Connector.open("file:///a:/");
                Enumeration en = fconn.list();
                while (en.hasMoreElements()) {
                    String name = (String) en.nextElement();

                    if ((name.indexOf("ukp") < 0) && (name.indexOf("Bootstrap") < 0)) {
                        FileConnection tmp = (FileConnection) Connector.open("file:///a:/" + name);
                        tmp.delete();
                        tmp.close();
                    } else {

                    }
                    answer[0] = 0;
                }
            }
        } catch (Exception ex) {
            Logger.ErrToLog("*RemoveAllFiles " + ex.getMessage());
        }

        return answer;
    }

    // экземпляры не создаются
    private Controller() {
    }

    public static byte[] Ping() {
        byte[] answer = {netAddr, extendedNetAddr};
        return answer;
    }

    public static byte[] OpenChannel(byte[] command) {
        byte[] answer = {0x5};
        if (command.length == 11) {
            Dialer.tryCounter = 0;
            // проверим пароль
            if ((new String(password).equals(new String(command, 3, 6))) | (new String(sprVsrPass).equals(new String(command, 3, 6)))) {
                SetChannelIsOpen();
                answer[0] = 0;
                currentMode = 0; // обычный режим                
            } else if (new String(masterPass).equals(new String(command, 3, 6))) {
                SetChannelIsOpen();
                answer[0] = 0;
                currentMode = 1; // мастер-режим                
            }
        }
        return answer;
    }

    public static byte[] ChangePassword(byte[] command) {
        byte[] answer = {0x1};
        if (command.length == 18) {
            // проверим старый пароль
            if ((new String(password).equals(new String(command, 4, 6))) | (new String(sprVsrPass).equals(new String(command, 4, 6)))) {
                for (int i = 0; i < 6; i++) {
                    password[i] = command[i + 10];
                }
                SaveConfig();
                answer[0] = 0;
            }
        }
        return answer;
    }

    public static byte[] GetTime() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[8];
            try {
                DateTime dt = DtService.GetDateTime(true);
                answer[0] = Global.ToDecBin(dt.second);
                answer[1] = Global.ToDecBin(dt.minute);
                answer[2] = Global.ToDecBin(dt.hour);
                answer[3] = Global.ToDecBin(dt.day);
                answer[4] = Global.ToDecBin(dt.day);
                answer[5] = Global.ToDecBin(dt.month);
                answer[6] = Global.ToDecBin((byte) (dt.year - 2000));
                answer[7] = (byte) 0;
            } catch (Exception ex) {
                Logger.ErrToLog("*GetTime: " + ex.toString());
            }
        }
        return answer;
    }

    public static byte[] GetVersion() {
        return version;
    }

    public static byte[] TimeSynchro() {
        byte[] answer = {0x5};
        return answer;
    }

    public static byte[] GetMasterAddr() {
        byte[] answer = {0x5};
//        if (channelIsOpen > 0) {
//            answer = new byte[2];
//            answer[0] = 0;
//            answer[1] = (byte) Mercury.GetMsterAddr();
//        }
        return answer;
    }

    public static byte[] GetPhone() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = phone;
        }
        return answer;
    }

    public static byte[] GetMercCount() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[2];
            answer[0] = 0;
            answer[1] = (byte) Mercury.mercuryList.size();
        }
        return answer;
    }

    public static byte[] SetPhone(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 15) {
                for (int i = 0; i < 11; i++) {
                    phone[i] = command[i + 2];
                    SaveConfig();
                    answer[0] = 0;
                }
            } else {
                answer[0] = 1;
            }
        }
        return answer;
    }

    public static byte[] SetNetAddr(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 5) {
                netAddr = command[2];
                SaveConfig();
                answer[0] = 0;
            } else if (command.length == 6) {
                netAddr = command[2];
                extendedNetAddr = command[3];
                SaveConfig();
                answer[0] = 0;
            } else {
                answer[0] = 1;
            }
        }
        return answer;
    }

    public static byte[] GetExtNetAddr(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[2];
            answer[0] = 0;
            answer[1] = extendedNetAddr;
        }
        return answer;
    }

    public static byte[] GetMercList() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[Mercury.mercuryList.size() + 1];
            answer[0] = 0;
            for (int i = 1; i < answer.length; i++) {
                Mercury m = (Mercury) Mercury.mercuryList.elementAt(i - 1);
                answer[i] = m.netAddr;
            }
        }
        return answer;
    }

    public static byte[] AddMerc(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 5) {
                //счетчики с нулевым адресом нельзя
                if (command[2] == 0) {
                    answer[0] = 2;
                } else // проверим, нет ли уже такого счётчика                                            
                {
                    if (Mercury.SearchMercury(command[2]) != null) {
                        answer[0] = 1;
                    } else {
                        // нет, такого счётчика нет
                        Mercury.AddMercury(command[2]);
                        Mercury.SaveConfig();
                        answer[0] = 0;
                    }
                }
            } else {
                answer[0] = 1;
            }
        }
        return answer;
    }

    public static byte[] DeleteMercury(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 5) {
                Mercury.DelMercury(command[2]);
                Mercury.SaveConfig();
                answer[0] = 0;
            } else {
                answer[0] = 1;
            }
        }
        return answer;
    }

    public static byte[] SetMercPassword(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 1;
            if (command.length == 11) {
                Mercury m = Mercury.SearchMercury(command[2]);
                if (m != null) {
                    for (int i = 0; i < 6; i++) {
                        m.password[i] = command[i + 3];
                    }
                    answer[0] = 0;
                    Mercury.SaveConfig();
                }
            }
        }
        return answer;
    }

    public static byte[] GetDialingRule() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[2];
            answer[0] = 0;
            answer[1] = toDialing;
        }
        return answer;
    }

    public static byte[] SetDialingRule(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 1;
            if (command.length == 5) {
                toDialing = command[2];
                answer[0] = 0;
                SaveConfig();
            }
        }
        return answer;
    }

    public static byte[] GetCurrentAnalog(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 1;
            if (command.length == 5) {
                Mercury m = Mercury.SearchMercury(command[2]);
                if (m != null) {
                    if (m.OpenChannel()) {
                        answer = new byte[18];
                        for (int i = 0; i < 18; i++) {
                            answer[i] = (byte) 0xE3;
                        }
                        // опросим счётчик!!!
                        if (m.GetI() & m.GetU()) {
                            m.InsertValues(answer, 0);
                        }
                    }
                }
            }
        }
        return answer;
    }

    public static byte[] GetEventsCount() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[2];
            answer[0] = 0;
            answer[1] = (byte) AnJournal.EventsCount();
        }
        return answer;
    }

    public static byte[] GetEvent(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 1;
            if (command.length == 5) {
                byte n = (byte) (command[2] - 1); // номер элемента
                if ((n >= 0) & (n < AnJournal.EventsCount())) {
                    answer = AnJournal.GetEvent(n);
                }
            }
        }
        return answer;
    }

    public static byte[] ClearEvents() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            AnJournal.Clear();
            answer[0] = 0;
        }
        return answer;
    }

    public static byte[] SetValueConditions(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 1;
            if (command.length == 11) {
                if (Mercury.SetValueConditions(command, command[1] == SET_MAX)) {
                    answer[0] = 0;
                }
            }
        }
        return answer;
    }

    public static byte[] SetZerroConditions(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 1;
            if (command.length == 7) {
                if (Mercury.SetZerroConditions(command)) {
                    answer[0] = 0;
                }
            }
        }
        return answer;
    }

    public static byte[] GetConditions(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            byte[] c = Mercury.GetConditions(command[2]);
            if (c != null) {
                answer = c;
            }
        }
        return answer;
    }

    public static byte[] OpenEnergy(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 1;
            if (command.length == 8) {
                byte recCount = Archiver.energy.ReadArchive(command[3], command[4], command[5], command[2]);
                answer = new byte[2];
                answer[0] = 0;
                answer[1] = recCount;
            }
        }
        return answer;
    }

    public static byte[] OpenProfile(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 1;
            if (command.length == 8) {
                byte recCount = Archiver.profile.ReadArchive(command[3], command[4], command[5], command[2]);
                answer = new byte[2];
                answer[0] = 0;
                answer[1] = recCount;
            }
        }
        return answer;
    }

    public static byte[] GetEnergyInfill() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = Archiver.energy.GetInfill();
        }
        return answer;
    }

    public static byte[] GetProfileInfill() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = Archiver.profile.GetInfill();
        }
        return answer;
    }

    public static byte[] GetEnergyRecord(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 1;
            if (command.length == 5) {
                answer = Archiver.energy.GetRecord(--command[2]);
            }
        }
        return answer;
    }

    public static byte[] GetProfileRecord(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 1;
            if (command.length == 5) {
                answer = Archiver.profile.GetRecord(--command[2]);
            }
        }
        return answer;
    }

    
   

    public static byte[] SetDiscretConditions(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            for (int i = 0; i < disConditions.length; i++) {
                disConditions[i] = 0;
            }
            for (int i = 0; i < command.length - 4; i++) {
                disConditions[i] = command[i + 2];
                answer[0] = 0;
                SaveConfig();
            }
        }
        return answer;
    }

    public static byte[] SetDestHost(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            try {
                byte[] dh = new byte[command.length - 4];
                System.arraycopy(command, 2, dh, 0, dh.length);
                destHost = Global.ToString(dh);
                answer[0] = 0;
                SaveConfig();
            } catch (Exception e) {
                answer[0] = 1;
            }
        }
        return answer;
    }

    public static byte[] GetDestHost() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (destHost != null) {
                if (destHost.length() > 2) {
                    answer = Global.StringToBytes(destHost);
                } else {
                    answer = Global.StringToBytes("unknown");
                }
            } else {
                answer = Global.StringToBytes("unknown");
            }
        }
        return answer;
    }

    public static byte[] SetConnProfile(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            try {
                byte[] dh = new byte[command.length - 4];
                System.arraycopy(command, 2, dh, 0, dh.length);
                connectionProfile = Global.ToString(dh);
                answer[0] = 0;
                SaveConfig();
            } catch (Exception e) {
                answer[0] = 1;
            }
        }
        return answer;
    }

    public static byte[] GetConnProfile() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (connectionProfile != null) {
                if (connectionProfile.length() > 3) {
                    answer = Global.StringToBytes(connectionProfile);
                } else {
                    answer = Global.StringToBytes("unknown");
                }
            } else {
                answer = Global.StringToBytes("unknown");
            }
        }
        return answer;
    }

    public static byte[] SetNetPass(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            try {
                byte[] dh = new byte[command.length - 4];
                System.arraycopy(command, 3, dh, 0, dh.length);
                netPassword = Global.ToString(dh);
                answer[0] = 0;
                SaveConfig();
            } catch (Exception e) {
                answer[0] = 1;
            }
        }
        return answer;
    }

    public static byte[] GetNetPass() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = Global.StringToBytes(netPassword);
        }
        return answer;
    }

    public static byte[] SetNetInterval(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 5) {
                webInterval = command[2];
                SaveConfig();
                answer[0] = 0;
            }
        }
        return answer;
    }

    public static byte[] GetNetInterval() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[2];
            answer[0] = 0;
            answer[1] = (byte) webInterval;
        }
        return answer;
    }

    public static byte[] SetUseSSL(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 5) {
                useSSL = command[2] == 1;
                SaveConfig();
                answer[0] = 0;
            }
        }
        return answer;
    }

    public static byte[] GetUseSSL() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[2];
            answer[0] = 0;
            answer[1] = useSSL ? (byte) 1 : (byte) 0;
        }
        return answer;
    }

    public static byte[] GetAutoParameters() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[autoParameters.length];
            for (int i = 0; i < autoParameters.length; i++) {
                answer[i] = autoParameters[i];
            }
        }
        return answer;
    }

    public static byte[] SetAutoParameters(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length - 4 == autoParameters.length) {
                for (int i = 0; i < autoParameters.length; i++) {
                    autoParameters[i] = command[i + 2];
                }
                SaveShedule();
                answer[0] = 0;
            }
        }
        return answer;
    }

    public static byte[] GetShedule() {
        byte[] answer = {0x5};
        try {
            if (channelIsOpen > 0) {
                if (shedule.size() > 0) {
                    answer = new byte[shedule.size()];
                    for (int i = 0; i < shedule.size(); i++) {
                        answer[i] = ((Byte) shedule.elementAt(i)).byteValue();
                    }
                } else {
                    answer[0] = 0;
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Controller[00016" + e.toString());
        }
        return answer;
    }

    public static byte[] SetShedule(byte[] command) {
        byte[] answer = {0x5};
        try {
            if (channelIsOpen > 0) {
                if (command.length > 4) {
                    Vector newShedule = new Vector();
                    for (int i = 0; i < command.length - 4; i++) {
                        newShedule.addElement(new Byte(command[i + 2]));
                    }
                    shedule = newShedule;

                    SaveShedule();
                    answer[0] = 0;
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Controller[0016" + e.toString());
        }
        return answer;
    }
   
    // обработка группы комманд для проверки баланса
    public static byte[] SendBalance(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            // получить текущий баланс (если есть)
            if (command[2] == 1) {
                answer = new byte[]{0x1};
                if (Controller.currentBalans.length() > 0) {
                    String s = Controller.timeStampOfBalans + "@" + Controller.currentBalans;
                    answer = Global.StringToBytes(s);
                }
            }
        }
        return answer;
    }

    public static byte[] ExecuteOneAtCommand(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[]{0x0};
            byte[] cmd = new byte[command.length - 3];
            System.arraycopy(command, 2, cmd, 0, command.length - 4);
            atCommandToExec = Global.ToString(cmd);
        }
        return answer;
    }

    public static byte[] DiskCleaning(byte[] command) {
        byte[] answer = {0x5};
        try {
            if (channelIsOpen > 0) {
                if (command.length == 5) {
                    if (command[2] == 1) {
                        Archiver.energy.ArchiveCollector();
                        Archiver.profile.ArchiveCollector();
                        answer[0] = 0;
                    }
                    if (command[2] == 2) {
                        Archiver.FullCleaning();
                        answer[0] = 0;
                    }
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Controller[00016" + e.toString());
        }
        return answer;
    }

    public static byte[] PowerGPIO5(byte[] command) {
        byte[] answer = {0x5};

        byte cmd = command[2];
        try {
            if (cmd == 1) {
                atc.send("at^ssio=4,1\r");
                Global.Delay(100);
                atc.send("at^ssio=4,0\r");
            }
            if (cmd == 2) {
                atc.send("at^ssio=4,1\r");
                Global.Delay(5000);
                atc.send("at^ssio=4,0\r");
            }
        } catch (Exception e) {
            answer[0] = 1;
        }
        return answer;
    }

    public static byte[] AddMerc200(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 8) {
                //счетчик с нулевым адресом нельзя
                if (command[2] == 0 && command[3] == 0 && command[4] == 0 && command[5] == 0) {
                    answer[0] = 2;
                } else {
                    Merc200.AddMerc200(command[2], command[3], command[4], command[5]);
                    Merc200.SaveMerc200Config();
                    answer[0] = 0;
                }
            }
        }
        return answer;
    }

    public static byte[] DelMerc200(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 8) {
                Merc200.DelMerc200(command[2], command[3], command[4], command[5]);
                Merc200.SaveMerc200Config();
                answer[0] = 0;
            }
        }
        return answer;
    }

    public static byte[] Merc200Cnt() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[2];
            answer[0] = 0;
            answer[1] = (byte) Merc200.merc200List.size();
        }
        return answer;
    }

    public static byte[] GetMerc200Info(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 5) {
                Merc200 m = (Merc200) Merc200.merc200List.elementAt(command[2]);
                answer = new byte[4];
                answer[0] = m.addr1;
                answer[1] = m.addr2;
                answer[2] = m.addr3;
                answer[3] = m.addr4;
            }
        }
        return answer;
    }

    //Включение нагрузки счетчика
    //5-cnannel is close, 1-invalid format, 2-counter not found 0-Ok
    public static byte[] PowerOn(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 0x1;
            if (command.length == 8) {
                answer[0] = Merc200.PoewerOn(command[2], command[3], command[4], command[5]);
            } else if (command.length == 5) {
                answer = new byte[]{Mercury.PowerOn(command[2])};
            }
        }
        return answer;
    }

    //5-cnannel is close, 1-invalid format, 2-counter not found 3-counter not answer 4-counter error 0-Ok (byte), state(byte)
    public static byte[] PowerOff(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 0x1;
            if (command.length == 8) {
                answer[0] = Merc200.PoewerOff(command[2], command[3], command[4], command[5]);
            } else if (command.length == 5) {
                answer = new byte[]{Mercury.PowerOff(command[2])};
            }
        }
        return answer;
    }

    //5-cnannel is close, 1-invalid format, 2-counter not found 3-counter not answer 4-counter error 0-Ok (byte), state(byte)
    public static byte[] PowerState(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer[0] = 0x1;
            if (command.length == 8) {
                answer = Merc200.GetState(command[2], command[3], command[4], command[5]);
            } else if (command.length == 5) {
                answer = Mercury.GetState(command[2]);
            }
        }
        return answer;
    }

    private static byte[] GetPhones() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            answer = new byte[SmsService.phones.size() * 12 + 1];
            answer[0] = 0;
            for (int i = 0; i < SmsService.phones.size(); i++) {
                byte[] p = Global.StringToBytes((String) SmsService.phones.elementAt(i));
                Global.CopyArray(answer, p, i * 12 + 1);
            }
        }
        return answer;
    }

    private static byte[] AddPhone(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 16) {
                byte[] p = new byte[]{
                    command[2], //+
                    command[3], //7
                    command[4], //9
                    command[5], //0
                    command[6], //3
                    command[7], //3
                    command[8], //0
                    command[9], //6
                    command[10], //1
                    command[11], //2 
                    command[12], //9
                    command[13] //7
                };
                String phone = Global.ToString(p);

                SmsService.phones.addElement(phone);
                SavePhones();
                answer[0] = 0;
            }
        }
        return answer;
    }

    private static byte[] RemovePhone(byte[] command) {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            if (command.length == 5) {
                try {
                    SmsService.phones.removeElementAt(command[2]);
                    SavePhones();
                } catch (Exception ex) {
                    answer[0] = 2;
                }
            } else {
                answer[0] = 1;
            }
        }
        return answer;
    }

    private static byte[] RemoveAllPhones() {
        byte[] answer = {0x5};
        if (channelIsOpen > 0) {
            SmsService.phones.removeAllElements();
            SavePhones();
            answer[0] = 0;
        }
        return answer;
    }
}
