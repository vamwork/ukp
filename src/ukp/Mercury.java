package ukp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;

import javax.microedition.io.file.FileConnection;

public class Mercury {

    private static final int defReadCounter = 3; // число попыток опроса

    // счётчика ведения профиля U & I
    public int kSch = -1; // постоянная счётчика

    public int firmwareVersion = -1; // версия ПО счетчика

    public static Vector mercuryList = new Vector(); // список счётчиков

    public static byte masterNetAddr = 0; // ведущий счётчик

    public Vector powerProfile; // массив записей профиля

    // параметры конфигурации для счётчика
    public byte netAddr;

    public byte[] password = {1, 1, 1, 1, 1, 1}; // defaul password

    public MercEvent mercEvent;

    public float maxU, maxI, minU, minI = 0;

    public boolean zrU, zrI = false; // фиксировать нули

    // "текущие" значения
    Unit currentI1 = new Unit();
    Unit currentI2 = new Unit();
    Unit currentI3 = new Unit();
    Unit currentU1 = new Unit();
    Unit currentU2 = new Unit();
    Unit currentU3 = new Unit();
    boolean uZerroFlag = false;     //используется для отправки sms при пропаже напряжения

    int testU = 0;

    public int profileReadCounter = 0; // счётчик попыток опроса счётчика для ведения профиля U & I
    public int monthEnergyReadCounter = 0; // счетчик для опроса "Энергия за предыдущий месяц"
    public int dayEnergyReadCounter = 0; // счетчик для опроса "Энергия за предыдущие сутки"
    public int sheduleEnergyReadCounter = 0; // счетчик для опроса по расписанию
    public int sheduleZoneEnergyReadCounter = 0; // счетчик для опроса по расписанию для тарифных зон
    public int sheduleTimeCorrect = 0; // счетчик для проверки веремени счетчиков
    public int sheduleEventsRead = 0; // счетчик для опроса событий
    public int sheduleAdditionalRead = 0; // сбор дополнительных параметров

    byte powerState = 2;   //состояние внутреннего реле нагрузки 2-не определено, 0-откл., 1-вкл.

    // профиля U & I
    // public boolean liveValues = false; //"живые" I U
    // //значения считаются живыми, если не было зафиксировано факта
    // исчерпывания попыток опроса
    // //для мастер-счётчика параметр liveValues устанавливается в false
    // так-же.
    // true ставится при регулярных опросах
    public static void PrepareMercInfo() {
        for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
            Mercury m = (Mercury) e.nextElement();
            int c = 3;
            while (c > 0) {
                c--;
                if (m.OpenChannel()) {
                    if (m.GetMercuryInfo()) {
                        break;
                    }
                }
            }
        }
    }

    private void AddConfiguration(OutputStream os) {
        try {
            os.write(netAddr);
            os.write(password);
            os.write(ToBytes(maxI, 1000));
            os.write(ToBytes(maxU, 100));
            os.write(ToBytes(minI, 1000));
            os.write(ToBytes(minU, 100));
            if (zrU) {
                os.write(1);
            } else {
                os.write(0);
            }
            if (zrI) {
                os.write(1);
            } else {
                os.write(0);
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0001]" + e.toString());
        }
    }

    private void ExtractFromConfiguration(InputStream is) throws IOException {
        netAddr = (byte) is.read();
        is.read(password, 0, 6);
        byte[] b = new byte[3];
        is.read(b, 0, 3);
        maxI = ToValue(b, 1000);
        is.read(b, 0, 3);
        maxU = ToValue(b, 100);
        is.read(b, 0, 3);
        minI = ToValue(b, 1000);
        is.read(b, 0, 3);
        minU = ToValue(b, 100);
        zrU = is.read() == 1;
        zrI = is.read() == 1;
    }

    private static float ToValue(byte b1, byte b2, byte b3, int prec) {
        float r = 0;
        short sb1 = (short) (Global.ToShort(b1) & (short) 0x3F);
        short sb2 = Global.ToShort(b2);
        short sb3 = Global.ToShort(b3);

        r = sb2 * 0x100 + sb3;
        r = (float) sb1 * ((float) 0x10000 / (float) prec) + (r / prec);
        return r;
    }

    private static float ToValue(byte[] b, int prec) {
        return ToValue(b[0], b[2], b[1], prec);
    }

    public static byte[] ToBytes(float v, int prec) {
        byte[] r = new byte[3];
        int i = (int) (v * prec);
        r[1] = (byte) (i - ((int) (i / 0x100)) * 0x100);
        i = i / 0x100;
        r[2] = (byte) (i - ((int) (i / 0x100)) * 0x100);
        i = i / 0x100;
        r[0] = (byte) i;
        return r;
    }

    // найти запись профиля мощности по времени
    private PowerProfileRecord GetProfileRecord(long time) {
        PowerProfileRecord result = null;
        for (int i = 0; i < powerProfile.size(); i++) {
            PowerProfileRecord record = (PowerProfileRecord) powerProfile.elementAt(i);
            if (record.dateTime == time) {
                result = record;
            }
        }
        return result;
    }

    // для всех счётчиков обновить массив профилей мощности
    // подготавливает "заготовки" для записей профиля мощности
    public static void PreparePowerProfiles() {
        try {

            if (Controller.autoParameters[2] == 1) {
                DateTime dt = DtService.GetDateTime(true);
                int day = dt.day;
                int month = dt.month;
                int year = dt.year;
                int hour = dt.hour;
                int minute = dt.minute;
                if (minute < 30) {
                    minute = 0;
                } else {
                    minute = 30;
                }

                long startTime = Global.timeAsMilliseconds(day, month, year, hour, minute);
                startTime = startTime + 3600000;

                for (int i = 0; i < mercuryList.size(); i++) {
                    Mercury m = (Mercury) mercuryList.elementAt(i);
                    m.PreparePowerProfileOfOneMerc(startTime);
                }
            }
        } catch (Exception ex) {
            Logger.ErrToLog("Mercury.Prepare power profile" + ex.toString());
        }
    }

    // добавить на обслуживание "получасовку" мощности
    public void PreparePowerProfileOfOneMerc(long startTime) {
        Vector newPowerProfile = new Vector();

        for (int i = 0; i < 24; i++) {
            PowerProfileRecord record = GetProfileRecord(startTime);
            if (record == null) {
                record = new PowerProfileRecord(startTime);
            }
            newPowerProfile.addElement(record);
            startTime = startTime - 1800000;
        }
        powerProfile = newPowerProfile;
        System.gc();
    }

    // опрос профиля мощности
    public static void ReadPowerProfiles() {
        PowerProfileRecord pRecord;
        int counter;
        Mercury m;

        for (int j = 0; j < mercuryList.size(); j++) {
            m = (Mercury) mercuryList.elementAt(j);
            counter = 3; // число попыток опроса текущего состояние профиля мощности
            boolean isNoPointer = false;
            // проверим, есть ли записи для которых не известны адреса
            for (int i = 0; i < m.powerProfile.size(); i++) {
                pRecord = (PowerProfileRecord) m.powerProfile.elementAt(i);
                if (pRecord.pointer < 0) {
                    isNoPointer = true;
                }
            }

            if (isNoPointer) {
                // в массиве профиля есть не установленные адреса
                // пытаемся прочитать текущее состояние профиля мощности
                // и установить адреса всех записей
                while ((counter > 0) & !m.GetLastPowerRecord()) {
                    counter--;
                }
            }
            if (counter > 0) {
                // удалось прочитать текущее состояние
                for (int i = 0; i < m.powerProfile.size(); i++) {
                    pRecord = (PowerProfileRecord) m.powerProfile.elementAt(i);
                    if ((pRecord.pointer > 0) & (!pRecord.isNotNull)) {
                        // для записи есть адрес и запись еще не была прочитана
                        m.ReadProfileRecord(pRecord);
                    }
                }
            }
        }
    }

    // прочитать запись профиля мощности из счётчика
    // pRecord "знает" свой адрес внутри адресного пространства счётчика
    public void ReadProfileRecord(PowerProfileRecord pRecord) {
        if (kSch > 0) {
            int hiAddr = pRecord.pointer / 0x100;
            int loAddr = pRecord.pointer - hiAddr * 0x100;

            byte b17 = 0x03;
            if (pRecord.bit17 != 0 && firmwareVersion > 6) {
                b17 = (byte) (b17 | (byte) 0x80);
            }

            byte[] command = {(byte) 0x6, b17, (byte) hiAddr, (byte) loAddr, (byte) 0x0F};

            byte[] answer = Command(command);

            if (answer.length == 15) {
                if (answer[3] == 0) {
                    pRecord.bad = 0;
                } else {
                    int day = Global.ToDec(answer[3]);
                    int month = Global.ToDec(answer[4]);
                    int year = Global.ToDec(answer[5]) + 2000;
                    int hour = Global.ToDec(answer[1]);
                    int minute = Global.ToDec(answer[2]);

                    minute = Global.AnyMinuteTo30Min(minute);

                    long recTime = Global.timeAsMilliseconds(day, month, year, hour, minute);
                    {
                        // временная метка совпала
                        pRecord.data = new byte[10];
                        System.arraycopy(answer, 7, pRecord.data, 0, 8);
                        // вставить обработку статусов
                        int k = kSch;
                        /*
		         * if (firmwareVersion > 6) { k = k / 2; }
                         */
                        int hiK = k / 0x100;
                        int loK = k - hiK * 0x100;
                        pRecord.data[8] = (byte) hiK;
                        pRecord.data[9] = (byte) loK;
                        pRecord.isNotNull = true;
                    }
                }
            }
        }
    }

    // создать Message's на основе еще не отправленных профилей мощности
    public static void GenerateMsgByPowerProfiles() {
        for (int i = 0; i < mercuryList.size(); i++) {
            Mercury m = (Mercury) mercuryList.elementAt(i);

            for (int j = 0; j < m.powerProfile.size(); j++) {
                PowerProfileRecord pRecord = (PowerProfileRecord) m.powerProfile.elementAt(j);
                if (pRecord.isNotNull & !pRecord.isSended & (pRecord.bad > 0)) {
                    // создадим Message и запустим поток, который будет пытаться занести его в очередь
                    Message msg = new Message(m.netAddr, (byte) 3, pRecord.day, pRecord.month, pRecord.year, pRecord.hour, pRecord.minute, 0, pRecord.data);
                    pRecord.isSended = true;
                    Service.AddMsgToOrder(msg);
                }
            }
        }
    }

    public Mercury() {
        netAddr = 0;
        // присвоим параметрам номера
        currentI1.parNumber = 1;
        currentI2.parNumber = 2;
        currentI3.parNumber = 3;
        currentU1.parNumber = 4;
        currentU2.parNumber = 5;
        currentU3.parNumber = 6;

        // создадим массив профилей
        powerProfile = new Vector();
        // создадим массив событий
        mercEvent = new MercEvent(this);
    }

    // отправить команду счётчику и предварительно проверить ответ
    // из ответа убирается CRC и адрес счётчика
    public byte[] Command(byte[] cmd) {
        // отправить cmd счётчику. К команде добавляется адрес счётчика и crc.
        // ответ возвращается проверенный и без адреса счётчика и crc
        // составим fullCommans с адресом счётчика и с crc
        byte[] fullCommand = Global.MakeCommand(cmd, netAddr);

        // отправим счётчику, получим ответ
        byte[] response = new byte[0];

        try {
            byte[] mercResp = Controller.cp.Request(fullCommand, 80);
            // проверим ответ
            // длина ответа д/б больше 3
            if (mercResp.length > 3) {
                // адрес в ответе должен совпадать с netAddr, либо запрашивался по "нулевому" адресу
                if (mercResp[0] == netAddr) {
                    // проверка crc
                    if (Global.CheckCRC(mercResp)) {
                        // ответ корректный
                        // проверим ответ счетчика
                        // если канал ошибка "канал не открыт" порпытаться открыть канал                        
                        response = new byte[mercResp.length - 3];
                        for (int i = 1; i < mercResp.length - 2; i++) {
                            response[i - 1] = mercResp[i];
                        }
                    }
                }
            }
        } catch (Exception e) {
            // нет ответа
            Logger.ErrToLog("*Mercury[0002]" + e.toString());
        }
        Global.Delay(150);
        return response;
    }

    // тройная попытка отправить команду
    public byte[] CommandRetry(byte[] cmd) {
        byte[] result = new byte[0];
        int cnt = 0;
        while (cnt < 3 & result.length < 3) {
            result = Command(cmd);
            cnt++;
            if (result.length < 3) {
                Global.Delay(150);
            }
        }
        return result;
    }

    // установить количество попыток опроса счётчиков для профиля и энергии
    public static void SetReadCounter() {
        if (Controller.autoParameters[0] == 1) {
            for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
                Mercury currentM = (Mercury) e.nextElement();
                currentM.profileReadCounter = defReadCounter;
            }
        }
    }

    // установить количество попыток опроса счётчиков для "Энергия за предыдущий месяц"
    public static void SetMonthEnergyReadCounter() {
        if (Controller.autoParameters[3] == 1) {
            for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
                Mercury currentM = (Mercury) e.nextElement();
                currentM.monthEnergyReadCounter = defReadCounter;
            }
        }
    }

    // установить количество попыток опроса счётчиков для "Энергия за предыдущие сутки"
    public static void SetDayEnergyReadCounter() {
        if (Controller.autoParameters[1] == 1) {
            for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
                Mercury currentM = (Mercury) e.nextElement();
                currentM.dayEnergyReadCounter = defReadCounter;
            }
        }
    }

    // установить количество попыток проверки и корректировки времени в счетчиках
    public static void SetSheduleTimeCorrect() {
        //if (Controller.autoParameters[6] == 1) {
        if (true) {
            for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
                Mercury currentM = (Mercury) e.nextElement();
                currentM.sheduleTimeCorrect = defReadCounter;
            }
        }
    }

    // установить количество попыток сбора событий счетчиков
    public static void SetSheduleEvents() {
        if (Controller.autoParameters[7] == 1) {
            for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
                Mercury currentM = (Mercury) e.nextElement();
                currentM.sheduleEventsRead = defReadCounter;
            }
        }
    }

    public static void SetSheduleAddtional() {
        if (Controller.autoParameters[8] == 1) {
            for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
                Mercury currentM = (Mercury) e.nextElement();
                currentM.sheduleAdditionalRead = defReadCounter;
            }
        }
    }

    // установить количество попыток опроса для "Энергия по расписанию"
    public static void SetSheduleEnergyReadCounter() {
        // счетчики для опросчиков суммарного расхода и расхода по тарифным зонам
        int readCounter = 0;
        int zoneReadCounter = 0;

        try {
            if (Controller.autoParameters[4] == 1) {
                readCounter = defReadCounter;
            }

            if (Controller.autoParameters[5] == 1) {
                zoneReadCounter = defReadCounter;
            }
            int prevDayOfMonth = Global.GetPrevDay();
            int d = 0;
            DateTime dt = DtService.GetDateTime(true);
            if (dt.day == 1) {
                // сегодня первый день месяца
                d = 32;
            }

            for (int i = 0; i < Controller.shedule.size(); i++) {
                byte s = ((Byte) Controller.shedule.elementAt(i)).byteValue();

                if (((s == prevDayOfMonth) || (s == d)) || s == 33) {
                    for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
                        Mercury currentM = (Mercury) e.nextElement();
                        currentM.sheduleEnergyReadCounter = readCounter;
                        currentM.sheduleZoneEnergyReadCounter = zoneReadCounter;
                    }
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0012]" + e.toString());
        }
    }

    // "вставить" в массив dest значения I & U
    public void InsertValues(byte[] dest, int pos) {
        try {
            Global.CopyArray(dest, ToBytes(currentI1.GetAverageValue(), 1000), pos);
            Global.CopyArray(dest, ToBytes(currentI2.GetAverageValue(), 1000), pos + 3);
            Global.CopyArray(dest, ToBytes(currentI3.GetAverageValue(), 1000), pos + 6);
            Global.CopyArray(dest, ToBytes(currentU1.GetAverageValue(), 100), pos + 9);
            Global.CopyArray(dest, ToBytes(currentU2.GetAverageValue(), 100), pos + 12);
            Global.CopyArray(dest, ToBytes(currentU3.GetAverageValue(), 100), pos + 15);
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0003]" + e.toString());
        }
    }

    // опрос U & I для всех счётчиков для наполнения усредненного массива
    public static void SborIU() {
        for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
            Mercury currentM = (Mercury) e.nextElement();
            if (currentM.OpenChannel()) {
                currentM.GetU();
                currentM.GetI();
            }
        }
    }

    // осуществить сбор U & I для профиля
    public static void SborExecute() {
        DateTime dt = DtService.GetDateTime(true);
        if ((mercuryList != null) && (mercuryList.size() > 0)) {
            for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
                Mercury currentM = (Mercury) e.nextElement();
                if (currentM.OpenChannel()) {
                    if (currentM.kSch < 0) {
                        currentM.GetMercuryInfo();
                    }
                    if (currentM.kSch >= 0) {

                        if (currentM.profileReadCounter > 0) {
                            currentM.profileReadCounter--;

                            if (currentM.GetI() & currentM.GetU()) {
                                // U & I успешно прочитаны
                                currentM.profileReadCounter = 0;
                                // сформируем запись для архива
                                //DateTime dt = IrzTimer.GetTime();
                                byte[] archRec = new byte[24];
                                archRec[0] = currentM.netAddr;
                                archRec[1] = dt.hour;
                                archRec[2] = dt.minute;
                                archRec[3] = dt.second;
                                currentM.InsertValues(archRec, 4);

                                // очистим массивы средних значений
                                currentM.currentI1.ClearAverageArray();
                                currentM.currentI2.ClearAverageArray();
                                currentM.currentI3.ClearAverageArray();
                                currentM.currentU1.ClearAverageArray();
                                currentM.currentU2.ClearAverageArray();
                                currentM.currentU3.ClearAverageArray();

                                archRec[22] = 0;
                                archRec[23] = 0;
                                Archiver.profile.SaveArchRecord(archRec);

                                // создадим Message и запустим поток, который будет пытаться занести его в очередь                            
                                Message msg = new Message(currentM.netAddr, (byte) 1, dt.day, dt.month, dt.year, dt.hour, dt.minute, dt.second, archRec);
                                Service.AddMsgToOrder(msg);
                            }
                        }

                        // Сбор параметра "Энергия за предыдущий месяц"
                        try {
                            if (currentM.monthEnergyReadCounter > 0) {
                                currentM.monthEnergyReadCounter--;
                                // необходимо прочитать энергию
                                // считаем, что канал со счётчиком открыт
                                int year = dt.year;
                                int month = dt.month;
                                int m;

                                if (month == 1) {
                                    m = 12;

                                } else {
                                    m = month - 1;
                                }

                                byte a = (byte) ((3 << 4) | m);

                                byte[] enrg = currentM.GetEnergy(a);
                                if (enrg != null) {
                                    // энергия успешно прочитана
                                    currentM.monthEnergyReadCounter = 0;
                                    byte[] enrgRec = new byte[84];
                                    enrgRec[0] = currentM.netAddr;
                                    enrgRec[1] = 0;
                                    enrgRec[2] = 0;
                                    enrgRec[3] = 0;
                                    System.arraycopy(enrg, 0, enrgRec, 4, enrg.length);
                                    Archiver.energy.SaveArchRecord(enrgRec);

                                    Message msg = new Message(currentM.netAddr, (byte) 4, 1, month, year, 0, 0, 0, enrgRec);
                                    Service.AddMsgToOrder(msg);
                                }
                            }
                        } catch (Exception ex) {
                            // нет ответа
                            Logger.ErrToLog("*Mercury[0010]" + ex.toString());
                        }

                        // Сбор параметра "Энергия за предыдущие сутки"
                        try {
                            if (currentM.dayEnergyReadCounter > 0) {
                                currentM.dayEnergyReadCounter--;
                                // необходимо прочитать энергию
                                // считаем, что канал со счётчиком открыт
                                byte[] enrg = currentM.GetEnergy((byte) 0x50);
                                if (enrg != null) {
                                    // энергия успешно прочитана
                                    currentM.dayEnergyReadCounter = 0;
                                    byte[] enrgRec = new byte[84];
                                    enrgRec[0] = currentM.netAddr;
                                    enrgRec[1] = 0;
                                    enrgRec[2] = 0;
                                    enrgRec[3] = 0;
                                    System.arraycopy(enrg, 0, enrgRec, 4, enrg.length);
                                    Archiver.energy.SaveArchRecord(enrgRec);

                                    Message msg = new Message(currentM.netAddr, (byte) 2, dt.day, dt.month, dt.year, 0, 0, 0, enrgRec);
                                    Service.AddMsgToOrder(msg);
                                }
                            }
                        } catch (Exception ex) {
                            // нет ответа
                            Logger.ErrToLog("*Mercury[0011]" + ex.toString());
                        }

                        // Сбор параметра "Суммарная энергия по расписанию"
                        try {
                            if (currentM.sheduleEnergyReadCounter > 0) {
                                currentM.sheduleEnergyReadCounter--;
                                byte[] enrg = currentM.GetUpToDayEnergy((byte) 6, (byte) 0xA6);
                                if (enrg != null) {
                                    currentM.sheduleEnergyReadCounter = 0;
                                    int hiK = currentM.kSch / 0x100;
                                    int loK = currentM.kSch - hiK * 0x100;

                                    byte[] enrgRec = new byte[22];
                                    enrgRec[0] = currentM.netAddr;
                                    enrgRec[1] = 0;
                                    enrgRec[2] = 0;
                                    enrgRec[3] = 0;
                                    System.arraycopy(enrg, 0, enrgRec, 4, enrg.length);
                                    enrgRec[20] = (byte) hiK;
                                    enrgRec[21] = (byte) loK;

                                    Message msg = new Message(currentM.netAddr, (byte) 5, dt.day, dt.month, dt.year, 0, 0, 0, enrgRec);
                                    Service.AddMsgToOrder(msg);
                                }
                            }
                        } catch (Exception ex) {
                            // нет ответа
                            Logger.ErrToLog("*Mercury[0011]" + ex.toString());
                        }

                        // Сбор параметра "Энергия по расписанию по тарифам"
                        try {
                            if (currentM.sheduleZoneEnergyReadCounter > 0) {
                                currentM.sheduleZoneEnergyReadCounter--;
                                byte[] enrg0 = currentM.GetUpToDayEnergy((byte) 6, (byte) 0xA6);
                                byte[] enrg1 = currentM.GetUpToDayEnergy((byte) 6, (byte) 0xB7);
                                byte[] enrg2 = currentM.GetUpToDayEnergy((byte) 6, (byte) 0xC8);
                                byte[] enrg3 = currentM.GetUpToDayEnergy((byte) 6, (byte) 0xD9);
                                byte[] enrg4 = currentM.GetUpToDayEnergy((byte) 6, (byte) 0xEA);

                                if ((enrg0 != null) & (enrg1 != null) & (enrg2 != null) & (enrg3 != null) & (enrg4 != null)) {
                                    currentM.sheduleZoneEnergyReadCounter = 0;
                                    int hiK = currentM.kSch / 0x100;
                                    int loK = currentM.kSch - hiK * 0x100;
                                    byte[] zoneEnrgRec = new byte[86];
                                    zoneEnrgRec[0] = currentM.netAddr;
                                    zoneEnrgRec[1] = 0;
                                    zoneEnrgRec[2] = 0;
                                    zoneEnrgRec[3] = 0;

                                    System.arraycopy(enrg0, 0, zoneEnrgRec, 4, 16);
                                    System.arraycopy(enrg1, 0, zoneEnrgRec, 20, 16);
                                    System.arraycopy(enrg2, 0, zoneEnrgRec, 36, 16);
                                    System.arraycopy(enrg3, 0, zoneEnrgRec, 52, 16);
                                    System.arraycopy(enrg4, 0, zoneEnrgRec, 68, 16);
                                    zoneEnrgRec[84] = (byte) hiK;
                                    zoneEnrgRec[85] = (byte) loK;
                                    Message msg = new Message(currentM.netAddr, (byte) 6, dt.day, dt.month, dt.year, 0, 0, 0, zoneEnrgRec);
                                    Service.AddMsgToOrder(msg);
                                }

                            }
                        } catch (Exception ex) {
                            // нет ответа
                            Logger.ErrToLog("*Mercury[0011]" + ex.toString());
                        }

                        // Корректировка времени счетчиков
                        if ((currentM.sheduleTimeCorrect > 0) && Controller.timeIsSynchro) {
                            currentM.sheduleTimeCorrect--;

                            long mercTime = currentM.GetMercuryTime();
                            DateTime cdt = DtService.GetDateTime(true);

                            long controllerTime = System.currentTimeMillis() + Controller.timeCorrector;

//                            //создадим событие - расхождение времени счетчика и контроллера
//                            Date d1 = new Date();
//                            d1.setTime(controllerTime);
//                            Calendar c1 = Calendar.getInstance();
//                            c1.setTime(d1);
//                            Date d2 = new Date();
//                            d2.setTime(mercTime);
//                            Calendar c2 = Calendar.getInstance();
//                            c2.setTime(d2);
//                            String dt1 = Global.GetCalendarAsDateTimeString(c1);
//                            String dt2 = Global.GetCalendarAsDateTimeString(c2);
//                            String eventBody = "006%" + String.valueOf(Global.ToShort(currentM.netAddr)) + "%" + dt1 + " - " + dt2 + "%";
//
//                            Event.AddEvent((byte) 1, eventBody);
//                            Event.SaveEvents();

                            if (Math.abs(mercTime - controllerTime) > TimeService.MAX_DELTA_TIME) {
                                // надо корретировать
                                // определим величину отклоненияесли больше 4 минут, корретируем на 4 минуты и генерим событе "PARTCORRENT"                            
                                String eventBody = "";
                                long timeToCounter;
                                if (Math.abs(mercTime - controllerTime) >= 240000) {
                                    eventBody = "005%" + String.valueOf(currentM.netAddr) + "%";
                                    if (mercTime > controllerTime) {
                                        timeToCounter = mercTime - 240000;
                                    } else {
                                        timeToCounter = mercTime + 240000;
                                    }
                                } else {
                                    eventBody = "004%" + String.valueOf(currentM.netAddr) + "%" + String.valueOf(mercTime - controllerTime) + "%";
                                    timeToCounter = controllerTime;
                                }

                                if (currentM.SetMercuryTime(timeToCounter)) {
                                    currentM.sheduleTimeCorrect = 0;
                                    Event.AddEvent((byte) 1, eventBody);
                                    Event.SaveEvents();
                                }
                            } else {
                                currentM.sheduleTimeCorrect = 0;
                            }
                        }

                        // чтение событий
                        if (currentM.sheduleEventsRead > 0) {
                            currentM.sheduleEventsRead--;
                            try {
                                currentM.mercEvent.ReadNewEvent();
                                currentM.mercEvent.MakeMessages();
                                currentM.sheduleEventsRead = 0;
                            } catch (Exception ex) {
                                Logger.ErrToLog("*Mercury[0014]" + ex.toString());
                            }
                        }

                        if (currentM.sheduleAdditionalRead > 0) {
                            currentM.sheduleAdditionalRead--;
                            try {
                                byte[] value = currentM.GetAdditional();
                                if (value != null) {

                                    byte[] aValue = new byte[84];
                                    aValue[0] = currentM.netAddr;
                                    aValue[1] = 0;
                                    aValue[2] = 0;
                                    aValue[3] = 0;
                                    System.arraycopy(value, 0, aValue, 4, 78);

                                    int hiK = currentM.kSch / 0x100;
                                    int loK = currentM.kSch - hiK * 0x100;

                                    aValue[82] = (byte) hiK;
                                    aValue[83] = (byte) loK;

                                    currentM.sheduleAdditionalRead = 0;
                                    Message msg = new Message(currentM.netAddr, (byte) 8, dt.day, dt.month, dt.year, dt.hour, dt.minute, 0, aValue);
                                    Service.AddMsgToOrder(msg);
                                }
                            } catch (Exception ex) {
                                Logger.ErrToLog("*Mercury[0014]" + ex.toString());
                            }
                        }
                    }
                }
            }
        }
    }

// добавить новый счётчик с адресом
    public static Mercury AddMercury(byte addr) {
        Mercury m = new Mercury();
        m.netAddr = addr;
        mercuryList.addElement(m);
        masterNetAddr = ((Mercury) (mercuryList.elementAt(0))).netAddr;
        return m;
    }

    // удалить счётчик с адресом
    public static void DelMercury(byte addr) {
        mercuryList.removeElement(SearchMercury(addr));
    }

    // возвращает счётчик с адресом, или пусто
    public static Mercury SearchMercury(byte addr) {
        Mercury m = null;
        for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
            Mercury currentM = (Mercury) e.nextElement();
            if (currentM.netAddr == addr) {
                m = currentM;
                break;
            }
        }
        return m;
    }

//    // мастер-счётчик, или пусто
//    public static Mercury GetMaster2() {
//        Mercury result = null;
//        try {
//            result = (Mercury) mercuryList.elementAt(0);
//        } catch (Exception e) {
//            Logger.AddToLog("*Mercury[0010]" + e.toString());
//        }
//        return result;
//    }
//
//    public static int GetMsterAddr2() {
//        int result = 0;
//        Mercury m = GetMaster();
//        if (m != null) {
//            result = m.netAddr;
//        }
//        return result;
//    }
    // количество счётчиков
    public static int MercCount() {
        return mercuryList.size();
    }

    // записать конфигурацию
    public static void SaveConfig() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + "mercury.cnf");

            if (!fConn.exists()) {
                fConn.create();
            }
            OutputStream os = fConn.openOutputStream(0);
            os.write(masterNetAddr); // запишем адрес ведущего счётчика
            for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
                Mercury m = (Mercury) e.nextElement();
                m.AddConfiguration(os);
            }
            os.close();
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*Mercury[0004]" + ex.toString());
        }
    }

    // прочитать конфигурацию
    public static void LoadConfig() {
        masterNetAddr = 0;
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + "mercury.cnf");

            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                is.mark(0);

                masterNetAddr = (byte) is.read();

                while (is.available() != 0) {
                    Mercury m = new Mercury();
                    m.ExtractFromConfiguration(is);

                    Mercury analog = Mercury.SearchMercury(m.netAddr);
                    if (analog == null && m.netAddr != 0) {
                        mercuryList.addElement(m);
                        m.mercEvent.LoadMercEvents();
                    }
                }
                masterNetAddr = ((Mercury) (mercuryList.elementAt(0))).netAddr;
                is.close();
            }
            fConn.close();

        } catch (Exception ex) {
            Logger.ErrToLog("*Mercury[0005]" + ex.toString());
        }
    }

    // открыть канал в счётчике
    public boolean OpenChannel() {
        boolean result = false;

        byte[] command = new byte[8];
        command[0] = 1;
        command[1] = 1;
        for (int i = 0; i < password.length; i++) {
            command[i + 2] = password[i];
        }

        byte[] answer = Command(command);
        Global.Delay(50);

        if (answer.length == 1) {
            if (answer[0] == 0) {
                result = true;
            }
        }
        return result;
    }

    private void Check(Unit unit, float max, float min, boolean zr) {
        float v = unit.value;
        if (v < 0) {
            v = -v;
        }

        int prevState = unit.state;
        if (v == 0) {
            // zerro
            unit.state = Unit.STATE_ZERRO;
        } else if (v < min) {
            // min
            unit.state = Unit.STATE_MIN;
        } else if (v > max) {
            // max
            unit.state = Unit.STATE_MAX;
        } else if ((v < max * 0.95) & (v > min * 1.05)) {
            unit.state = Unit.STATE_NORMAL;
        }
        if ((prevState != unit.state) & (prevState != Unit.STATE_NONE)) {
            // произошло изменение состояния, причём предыдущее состояние было не NONE            
            if ((unit.state == Unit.STATE_ZERRO) & zr) {
                AnJournal.AddEvent(this, unit.parNumber, (byte) 3);
                uZerroFlag = true;
            }
            if ((unit.state == Unit.STATE_MAX) & (max > 0)) {
                AnJournal.AddEvent(this, unit.parNumber, (byte) 1);
            }
            if ((unit.state == Unit.STATE_MIN) & (min > 0)) {
                AnJournal.AddEvent(this, unit.parNumber, (byte) 2);
            }
        }
    }

    // прочитать "энергия на начало суток"
    public byte[] GetUpToDayEnergy(byte hiAddr, byte loAddr) {
        byte[] result = new byte[16];
        try {
            byte[] command = {(byte) 0x6, (byte) 0x2, hiAddr, loAddr, (byte) 0x10};
            result = CommandRetry(command);
            if (result.length != 16) {
                result = null;
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0014]" + e.toString());
            result = null;
        }
        return result;
    }

    public byte[] GetAdditional() {
        byte[] result = new byte[78];

        int cnt = 0; // счетчик загруженных параметров (всего должно быть 8)

        byte[] command;
        byte[] answer;

        try {
            // -0.0- Активная мощность по фазам
            command = new byte[]{(byte) 0x8, (byte) 0x16, (byte) 0x00};
            answer = CommandRetry(command);

            if (answer.length == 12) {
                System.arraycopy(answer, 0, result, 0, 12);
                cnt++;

                // -0.1- Реативная мощность по фазам
                command = new byte[]{(byte) 0x8, (byte) 0x16, (byte) 0x4};
                answer = CommandRetry(command);

                if (answer.length == 12) {
                    System.arraycopy(answer, 0, result, 12, 12);
                    cnt++;

                    // -1.1- Полная мощность по фазам
                    command = new byte[]{(byte) 0x8, (byte) 0x16, (byte) 0x8};
                    answer = CommandRetry(command);

                    if (answer.length == 12) {
                        System.arraycopy(answer, 0, result, 24, 12);
                        cnt++;

                        // -3- Коэффициент мощности по фазам
                        command = new byte[]{(byte) 0x8, (byte) 0x16,
                            (byte) 0x30};
                        answer = CommandRetry(command);
                        if (answer.length == 12) {
                            System.arraycopy(answer, 0, result, 36, 12);
                            cnt++;

                            // -1- Напряжение по фазам
                            command = new byte[]{(byte) 0x8, (byte) 0x16, (byte) 0x11};
                            answer = CommandRetry(command);

                            if (answer.length == 9) {
                                System.arraycopy(answer, 0, result, 48, 9);
                                cnt++;

                                // -2- Ток по фазам
                                command = new byte[]{(byte) 0x8, (byte) 0x16, (byte) 0x21};
                                answer = CommandRetry(command);
                                if (answer.length == 9) {
                                    System.arraycopy(answer, 0, result, 57, 9);
                                    cnt++;

                                    // -4- Частота сети
                                    command = new byte[]{(byte) 0x8, (byte) 0x16, (byte) 0x40};
                                    answer = CommandRetry(command);
                                    if (answer.length == 3) {
                                        System.arraycopy(answer, 0, result, 66, 3);
                                        cnt++;

                                        // -5- Угол между фазными напряжениями
                                        command = new byte[]{(byte) 0x8, (byte) 0x16, (byte) 0x51};
                                        answer = CommandRetry(command);
                                        if (answer.length == 9) {
                                            System.arraycopy(answer, 0, result, 69, 9);
                                            cnt++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0016]" + e.toString());
        }

        if (cnt < 8) {
            result = null;
        }

        return result;
    }

    // считывает данные из массивов накопленной энергии arrayNumber - № массива (табл.4 стр.30)    
    // для чтения энергии за месяц указывается два полубайта
    public byte[] GetEnergy(byte arrayNumber) {
        byte[] result = new byte[80];

        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) 0xFF;
        }

        boolean[] isOk = new boolean[5];
        for (int i = 0; i < 5; i++) {
            isOk[i] = false;
        }

        int cntr = 3; // счётчик попыток
        boolean ok = false;

        try {
            while (!ok & cntr > 0) {
                cntr--;
                ok = true;
                for (int i = 0; i < 5; i++) {
                    if (!isOk[i]) {
                        byte[] command = {(byte) 0x5, arrayNumber, (byte) i};

                        byte[] answer = Command(command);

                        if (answer.length == 16) {
                            System.arraycopy(answer, 0, result, i * answer.length, answer.length);
                            isOk[i] = true;
                        } else {
                            ok = false;
                        }
                    }
                }
            }

        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0009]" + e.toString());
        }

        // если хотя бы один параметр не загружен - "сбросить" массив
        for (int i = 0; i < 5; i++) {
            if (!isOk[i]) {
                result = null;
            }
        }
        return result;
    }

    // прочитать вариант исполнения счётчика
    public boolean GetMercuryInfo() {
        boolean result = false;
        Global.Delay(150);
        try {
            byte[] command = {(byte) 0x8, (byte) 0x03};
            byte[] answer = Command(command);

            if (answer.length == 3) {

                firmwareVersion = answer[0];

                command[1] = (byte) 0x12;
                answer = Command(command);

                if (answer.length == 6) {
                    result = true;

                    int k = answer[1] & 0xF;
                    switch (k) {
                        case 0:
                            kSch = 5000;
                            break;
                        case 1:
                            kSch = 2500;
                            break;
                        case 2:
                            kSch = 1250;
                            break;
                        case 3:
                            kSch = 500;
                            break;
                        case 4:
                            kSch = 1000;
                            break;
                        case 5:
                            kSch = 250;
                            break;
                    }
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0009]" + e.toString());
        }

        Global.Delay(150);
        return result;
    }

    public boolean GetU() {
        boolean result = false;
        try {
            byte[] command = {(byte) 0x8, (byte) 0x16, (byte) 0x11};
            byte[] answer = Command(command);

            if (answer.length == 9) {

                currentU1.AddValue(ToValue(answer[0], answer[2], answer[1], 100));
                currentU2.AddValue(ToValue(answer[3], answer[5], answer[4], 100));
                currentU3.AddValue(ToValue(answer[6], answer[8], answer[7], 100));

                result = true;

                if (currentU1.state == Unit.STATE_NOREC) {
                    currentU1.state = Unit.STATE_NONE;
                    currentU2.state = Unit.STATE_NONE;
                    currentU3.state = Unit.STATE_NONE;
                } else {
                    // проверка уровней Unit unit, float max, float min, boolean zr) {
                    Check(currentU1, maxU, minU, zrU);
                    Check(currentU2, maxU, minU, zrU);
                    Check(currentU3, maxU, minU, zrU);

                    if (uZerroFlag) {
                        SmsService.MiltiSend(DtService.GetDateTime(true).toString() + " VNIMANIE! U=0.");
                        uZerroFlag = false;
                    }
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0006]" + e.toString());
        }

        return result;
    }

    // Чтение параметров последней записи средних мощностей  и установка адресов для всех заготовок профиля мощности
    private boolean GetLastPowerRecord() {
        boolean result = false;
        try {
            byte[] command = {(byte) 0x8, (byte) 0x13};
            byte[] answer = Command(command);

            if (answer.length == 9) {
                result = true;
                // выделим время последней записи
                int day = Global.ToDec(answer[5]);
                int month = Global.ToDec(answer[6]);
                int year = Global.ToDec(answer[7]) + 2000;
                int hour = Global.ToDec(answer[3]);
                int minute = Global.ToDec(answer[4]);

                minute = Global.AnyMinuteTo30Min(minute);

                // перевредем в млс.
                long time = Global.timeAsMilliseconds(day, month, year, hour, minute);

                // выделим адрес в зависимости от типа счётчика
                byte hi = answer[0];
                byte lo = answer[1];

                byte b17 = 0;
                if ((hi & (byte) 0xF0) != 0) {
                    b17 = 1;
                }

                if (firmwareVersion > 6) {
                    hi = (byte) ((byte) (hi << 4) | (byte) ((byte) (lo >> 4) & (byte) 0xF));
                    lo = (byte) (lo << 4);
                }

                int pointer = Global.ToShort(hi) * 0x100 + Global.ToShort(lo);

                // найдем подходящую запись в массиве прoфиля мощности
                for (int i = 0; i < powerProfile.size(); i++) {
                    PowerProfileRecord pRecord = (PowerProfileRecord) powerProfile.elementAt(i);
                    if (pRecord.dateTime == time) {
                        pRecord.pointer = pointer;
                        pRecord.bit17 = b17;
                        // теперь пройдемся вверх по всем записям и установим адрес
                        if (i < 48) {
                            int newPointer;
                            for (int j = i + 1; j < powerProfile.size(); j++) {
                                newPointer = pointer - (j - i) * 0x10;
                                if (newPointer < 0) {
                                    newPointer = newPointer + 0xFFFF;
                                }
                                pRecord = (PowerProfileRecord) powerProfile.elementAt(j);
                                pRecord.pointer = newPointer;
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0009]" + e.toString());
        }
        return result;
    }

    public boolean GetI() {
        boolean result = false;
        try {
            byte[] command = {(byte) 0x8, (byte) 0x16, (byte) 0x21};
            byte[] answer = Command(command);

            if (answer.length == 9) {
                currentI1.AddValue(ToValue(answer[0], answer[2], answer[1], 1000));
                currentI2.AddValue(ToValue(answer[3], answer[5], answer[4], 1000));
                currentI3.AddValue(ToValue(answer[6], answer[8], answer[7], 1000));

                result = true;
                if (currentI1.state == Unit.STATE_NOREC) {
                    currentI1.state = Unit.STATE_NONE;
                    currentI2.state = Unit.STATE_NONE;
                    currentI3.state = Unit.STATE_NONE;
                } else {
                    // проверка уровней Unit unit, float max, float min, boolean
                    // zr) {
                    Check(currentI1, maxI, minI, zrI);
                    Check(currentI2, maxI, minI, zrI);
                    Check(currentI3, maxI, minI, zrI);
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0007]" + e.toString());
        }
        return result;
    }

    public long GetMercuryTime() {
        long result = 0;
        try {
            if (OpenChannel()) {
                byte[] command = {(byte) 0x4, (byte) 0x0};
                byte[] answer = Command(command);
                if (answer.length == 8) {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.SECOND, Global.ToDec(answer[0]));
                    c.set(Calendar.MINUTE, Global.ToDec(answer[1]));
                    c.set(Calendar.HOUR_OF_DAY, Global.ToDec(answer[2]));

                    c.set(Calendar.DAY_OF_MONTH, Global.ToDec(answer[4]));
                    c.set(Calendar.MONTH, Global.ToDec(answer[5]) - 1);
                    c.set(Calendar.YEAR, Global.ToDec(answer[6]) + 2000);
                    result = c.getTime().getTime();
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0008]" + e.toString());
        }
        return result;
    }

    public boolean SetMercuryTime(long newTime) {
        boolean result = false;
        DateTime td = new DateTime(newTime);

        try {
            byte[] command = {(byte) 3, (byte) 0x0D,
                Global.ToDecBin(td.second), Global.ToDecBin(td.minute),
                Global.ToDecBin(td.hour)};
            byte[] answer = Command(command);

            if (answer.length == 1) {
                if (answer[0] == 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Mercury[0011]" + e.toString());
        }
        return result;
    }

    // прочитать событие
    public byte[] ReadEvent(byte parNumber, byte recordNumber) {
        byte[] result = null;
        byte[] command = {(byte) 4, parNumber, recordNumber};
        byte[] answer = Command(command);

        if (answer.length > 5) {
            result = answer;
        }

        return result;
    }

    // установить условия
    // процедура фактически обрабатывает две команды SetMax & SetMin
    public static boolean SetValueConditions(byte[] command, boolean setMax) {
        boolean result = false;
        byte mercAddr = command[2];
        // ищем счётчик, для которого устанавливаем условия
        Mercury m = Mercury.SearchMercury(mercAddr);
        if (m != null) {
            float i = ToValue(command[3], command[5], command[4], 1000);
            float u = ToValue(command[6], command[8], command[7], 100);
            // определим то это SetMax or SetMin
            if (setMax) {
                m.maxI = i;
                m.maxU = u;
            } else {
                m.minI = i;
                m.minU = u;
            }
            SaveConfig();
            result = true;
        }
        return result;
    }

    public static boolean SetZerroConditions(byte[] command) {
        boolean result = false;
        byte mercAddr = command[2];
        // ищем счётчик, для которого устанавливаем условия
        Mercury m = Mercury.SearchMercury(mercAddr);
        if (m != null) {
            m.zrI = command[3] == 1;
            m.zrU = command[4] == 1;
            Mercury.SaveConfig();
            result = true;
        }
        return result;
    }

    public static byte[] GetConditions(byte mercAddr) {
        byte[] result = null;
        // ищем счётчик, для которого устанавливаем условия
        Mercury m = Mercury.SearchMercury(mercAddr);
        if (m != null) {
            result = new byte[14];
            byte[] c;
            c = ToBytes(m.maxI, 1000);
            result[0] = c[0];
            result[1] = c[1];
            result[2] = c[2];
            c = ToBytes(m.maxU, 100);
            result[3] = c[0];
            result[4] = c[1];
            result[5] = c[2];
            c = ToBytes(m.minI, 1000);
            result[6] = c[0];
            result[7] = c[1];
            result[8] = c[2];
            c = ToBytes(m.minU, 100);
            result[9] = c[0];
            result[10] = c[1];
            result[11] = c[2];
            result[12] = (byte) (m.zrI ? 1 : 0);
            result[13] = (byte) (m.zrU ? 1 : 0);
        }
        return result;
    }

    public static String GetMercReport() {
        StringBuffer result = new StringBuffer("");
        for (Enumeration e = mercuryList.elements(); e.hasMoreElements();) {
            Mercury m = (Mercury) e.nextElement();
            result = result.append(String.valueOf(m.netAddr) + " ");
        }
        return result.toString();
    }

    public static byte PowerOn(byte addr) {
        byte result = 2;
        Mercury m = SearchMercury(addr);
        if (m != null) {
            byte[] answer = m.Command(new byte[]{0x3, 0x31, 0x0});
            if (answer != null) {
                if (answer[0] == 3) {
                    result = 0;
                    m.powerState = 1;
                } else {
                    result = 4;  //counter error
                }
            }
        } else {
            result = 3;  //counter not answer
        }
        return result;
    }

    public static byte PowerOff(byte addr) {
        byte result = 2;
        Mercury m = SearchMercury(addr);
        if (m != null) {
            byte[] answer = m.Command(new byte[]{0x3, 0x31, 0x1});
            if (answer != null) {
                if (answer[0] == 3) {
                    result = 0;
                    m.powerState = 0;
                } else {
                    result = 4;  //counter error
                }
            }
        } else {
            result = 3;  //counter not answer
        }
        return result;
    }

    public static byte[] GetState(byte addr) {
        byte[] result = {2};  //2-counter not found                
        Mercury m = SearchMercury(addr);
        if (m != null) {
            result = new byte[]{0, m.powerState};
        }
        return result;
    }
}
