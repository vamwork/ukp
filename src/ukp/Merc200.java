package ukp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class Merc200 {

    public static Vector merc200List = new Vector(); // список счётчиков    

    public int kSch = -1; // постоянная счётчика
    byte addr1 = 0;
    byte addr2 = 0;
    byte addr3 = 0;
    byte addr4 = 0;

    byte powerState = 2;   //состояние внутреннего реле нагрузки 2-не определено, 0-откл., 1-вкл.

    public static int defReadCounter = 3;

    public int powerReadCounter = 0; // счётчик попыток опроса текущей мощности
    public int energyReadCounter = 0; // счётчик попыток опроса энергии
    public int profileReadCounter = 0; // счётчик попыток опроса профиля мощности
    private int[] prevEnergy = {0, 0, 0, 0, 0}; // используется при эмуляции профиля мощности

    // добавить новый счётчик
    public static Merc200 AddMerc200(byte addr1, byte addr2, byte addr3, byte addr4) {
        Merc200 m = new Merc200();
        m.addr1 = addr1;
        m.addr2 = addr2;
        m.addr3 = addr3;
        m.addr4 = addr4;

        if (SearchMerc200(m.addr1, m.addr2, m.addr3, m.addr4) == null) {
            merc200List.addElement(m);
        } else {

        }
        return m;
    }

    public static void DelMerc200(byte addr1, byte addr2, byte addr3, byte addr4) {
        merc200List.removeElement(SearchMerc200(addr1, addr2, addr3, addr4));
    }

    public static Merc200 SearchMerc200(byte addr1, byte addr2, byte addr3, byte addr4) {
        Merc200 m = null;
        for (Enumeration e = merc200List.elements(); e.hasMoreElements();) {
            Merc200 currentM = (Merc200) e.nextElement();
            if ((currentM.addr1 == addr1) && (currentM.addr2 == addr2) && (currentM.addr3 == addr3) && (currentM.addr4 == addr4)) {
                m = currentM;
                break;
            }
        }
        return m;
    }

    // запись информации о счетчике в поток
    private void AddConfiguration(OutputStream os) {
        try {
            os.write(addr1);
            os.write(addr2);
            os.write(addr3);
            os.write(addr4);
        } catch (Exception e) {
            Logger.ErrToLog("*Merc200[0001]" + e.toString());
        }
    }

    // чтение информации о счетчике из потока
    private void ExtractFromConfiguration(InputStream is) throws IOException {
        {
            addr1 = (byte) is.read();
            addr2 = (byte) is.read();
            addr3 = (byte) is.read();
            addr4 = (byte) is.read();
        }
    }

    // записать конфигурацию
    public static void SaveMerc200Config() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + "merc200.cnf");

            if (!fConn.exists()) {
                fConn.create();
            }

            OutputStream os = fConn.openOutputStream(0);

            for (Enumeration e = merc200List.elements(); e.hasMoreElements();) {
                Merc200 m = (Merc200) e.nextElement();
                m.AddConfiguration(os);
            }
            os.close();
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*Merc200[0004]" + ex.toString());
        }
    }

    // прочитать конфигурацию
    public static void LoadConfig() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + "merc200.cnf");

            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                while (is.available() != 0) {
                    Merc200 m = new Merc200();
                    m.ExtractFromConfiguration(is);
                    Merc200 analog = Merc200.SearchMerc200(m.addr1, m.addr2, m.addr3, m.addr4);
                    if (analog == null) {
                        if (m.addr1 != 0 || m.addr2 != 0 || m.addr3 != 0 || m.addr4 != 0) {
                            merc200List.addElement(m);
                        }                        
                    }
                }
                is.close();
            }
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*Mercury200[0005]" + ex.toString());
        }
    }

// установить количество попыток опроса накопленной энергии
    public static void SetEnergyReadCounter() {
        for (Enumeration e = merc200List.elements(); e.hasMoreElements();) {
            Merc200 currentM = (Merc200) e.nextElement();
            currentM.energyReadCounter = defReadCounter;
        }
    }

    // установить количество попыток опроса текущей мощности
    public static void SetPowerReadCounter() {
        for (Enumeration e = merc200List.elements(); e.hasMoreElements();) {
            Merc200 currentM = (Merc200) e.nextElement();
            currentM.powerReadCounter = defReadCounter;
        }
    }

    // установить количество попыток опроса профиля мщности (эмулированного профиля)
    public static void SetProfileReadCounter() {
        for (Enumeration e = merc200List.elements(); e.hasMoreElements();) {
            Merc200 currentM = (Merc200) e.nextElement();
            currentM.profileReadCounter = defReadCounter;
        }
    }

    public static void SborExecute() {
        DateTime dt = DtService.GetDateTime(true);
        for (Enumeration e = merc200List.elements(); e.hasMoreElements();) {
            Merc200 currentM = (Merc200) e.nextElement();

            if (currentM.profileReadCounter > 0) {
                currentM.profileReadCounter--;

                byte[] energy = currentM.GetEnergy();
                if (energy != null) {
                    currentM.energyReadCounter = 0;

                    int[] values = new int[5];
                    values[0] = (energy[0] * 1000000 + energy[1] * 10000 + energy[2] * 100 + energy[3]); // тариф1
                    values[1] = (energy[4] * 1000000 + energy[5] * 10000 + energy[6] * 100 + energy[7]); // тариф2
                    values[2] = (energy[8] * 1000000 + energy[9] * 10000 + energy[10] * 100 + energy[11]); // тариф3
                    values[3] = (energy[12] * 1000000 + energy[13] * 10000 + energy[14] * 100 + energy[15]); // тариф4
                    values[4] = values[0] + values[1] + values[2] + values[3];

                    if (currentM.prevEnergy[0] > 0) {
                        int d1 = values[0] - currentM.prevEnergy[0];
                        if (d1 < 0) {
                            d1 = d1 + 0x99999999;
                        }
                        int d2 = values[0] - currentM.prevEnergy[0];
                        if (d2 < 0) {
                            d2 = d2 + 0x99999999;
                        }
                        int d3 = values[0] - currentM.prevEnergy[0];
                        if (d3 < 0) {
                            d3 = d3 + 0x99999999;
                        }
                        int d4 = values[0] - currentM.prevEnergy[0];
                        if (d4 < 0) {
                            d4 = d4 + 0x99999999;
                        }
                        int d0 = d1 + d2 + d3 + d4;

                        byte[] energyData = new byte[20];
                        energyData[0] = currentM.addr1;
                        energyData[1] = currentM.addr2;
                        energyData[2] = currentM.addr3;
                        energyData[3] = currentM.addr4;

                        System.arraycopy(energy, 0, energyData, 4, 16);
                        Message msg = new Message((byte) 0, (byte) 201, dt.day, dt.month, dt.year, dt.hour, dt.minute, 0, energyData);
                        Service.AddMsgToOrder(msg);

                    } else {
                        currentM.prevEnergy = values;
                    }
                }
            }

            if (currentM.powerReadCounter > 0) {
                currentM.powerReadCounter--;

                byte[] power = currentM.GetPower();
                if (power != null) {
                    // мощность успешно прочитана
                    currentM.powerReadCounter = 0;

                    byte[] powerData = new byte[11];
                    powerData[0] = currentM.addr1;
                    powerData[1] = currentM.addr2;
                    powerData[2] = currentM.addr3;
                    powerData[3] = currentM.addr4;

                    System.arraycopy(power, 0, powerData, 4, power.length);

                    Message msg = new Message((byte) 0, (byte) 200, dt.day, dt.month, dt.year, dt.hour, dt.minute, 0, powerData);
                    Service.AddMsgToOrder(msg);
                }
            }

            if (currentM.energyReadCounter > 0) {
                currentM.energyReadCounter--;

                byte[] energy = currentM.GetEnergy();
                if (energy != null) {
                    currentM.energyReadCounter = 0;

                    byte[] energyData = new byte[20];
                    energyData[0] = currentM.addr1;
                    energyData[1] = currentM.addr2;
                    energyData[2] = currentM.addr3;
                    energyData[3] = currentM.addr4;

                    System.arraycopy(energy, 0, energyData, 4, 16);

                    Message msg = new Message((byte) 0, (byte) 201, dt.day, dt.month, dt.year, dt.hour, dt.minute, 0, energyData);
                    Service.AddMsgToOrder(msg);
                }
            }
        }
    }

    // итоговый ответ содержит только полезную информацию (без адреса, команды и crc)
    public byte[] CleanCmd200(byte[] cmd) {
        byte[] response = null;
        byte[] fullResponse = Cmd200(cmd);

        if (fullResponse.length > 0) {
            response = new byte[fullResponse.length - 1];
            System.arraycopy(fullResponse, 1, response, 0, response.length);
        }
        return response;
    }

    // итоговый ответ содержит только команду, и данные
    public byte[] Cmd200(byte[] cmd) {
        byte[] response = new byte[0];
        try {
            byte[] cmdPart = new byte[cmd.length + 3];
            cmdPart[0] = addr2;
            cmdPart[1] = addr3;
            cmdPart[2] = addr4;
            System.arraycopy(cmd, 0, cmdPart, 3, cmd.length);
            byte[] fullCommand = Global.MakeCommand(cmdPart, addr1);

            //byte[] fullCommand = Global.MakeCommand(new byte[]{addr2, addr3, addr4, cmd}, addr1);
            byte[] mercResp = Controller.cp.Request(fullCommand, 50);

            // проверим ответ длина ответа д/б больше 3            
            if (mercResp.length > 3) {
                // адрес в ответе должен совпадать с addr1, addr2, addr3
                if ((mercResp[0] == addr1) && (mercResp[1] == addr2) && (mercResp[2] == addr3) && (mercResp[3] == addr4)) {
                    // проверка crc
                    if (Global.CheckCRC(mercResp)) {
                        // ответ корректный                        
                        response = new byte[mercResp.length - 6];
                        for (int i = 4; i < mercResp.length - 2; i++) {
                            response[i - 4] = mercResp[i];
                        }
                    }
                }
            }
        } catch (Exception e) {
            // нет ответа
            Logger.ErrToLog("*Merc200[0002]" + e.toString());
        }
        Global.Delay(150);
        return response;
    }

    public byte[] GetPower() {
        byte[] answer = CleanCmd200(new byte[]{(byte) 0x63});
        return answer;
    }

    public byte[] GetEnergy() {
        byte[] answer = CleanCmd200(new byte[]{(byte) 0x27});
        return answer;
    }

    public static long GetMasterTime() {
        long result = 0;
        Merc200 m = (Merc200) Merc200.merc200List.elementAt(0);
        if (m != null) {
            result = m.GetMerc200Time();
        }
        return result;
    }

    public long GetMerc200Time() {
        long result = 0;
        try {
            byte[] answer = CleanCmd200(new byte[]{(byte) 0x21});
            if (answer.length == 7) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.SECOND, Global.ToDec(answer[3]));
                c.set(Calendar.MINUTE, Global.ToDec(answer[2]));
                c.set(Calendar.HOUR_OF_DAY, Global.ToDec(answer[1]));

                c.set(Calendar.DAY_OF_MONTH, Global.ToDec(answer[4]));
                c.set(Calendar.MONTH, Global.ToDec(answer[5]) - 1);
                c.set(Calendar.YEAR, Global.ToDec(answer[6]) + 2000);
                result = c.getTime().getTime();
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Merc200[0008]" + e.toString());
        }
        return result;
    }

    public static byte PoewerOn(byte addr1, byte addr2, byte addr3, byte addr4) {
        byte result = 2;
        try {
            Merc200 m = Merc200.SearchMerc200(addr1, addr2, addr3, addr4);
            if (m != null) {
                m.powerState = 2; //состояние реле нагрузки НЕ ОПРЕДЕЛЕНО
                byte[] answer = m.Cmd200(new byte[]{(byte) 0x71, (byte) 0xFF});
                if (answer[0] == 0x71) {
                    answer = m.Cmd200(new byte[]{(byte) 0x71, (byte) 0x5a});
                    if (answer[0] == 0x71) {
                        result = 0;
                        m.powerState = 1;  //состояние реле нагрузки ВКЛЮЧЕНО
                    } else {
                        result = 4;  //counter error
                    }
                } else {
                    result = 4;  //counter error
                }
            } else {
                result = 3;  //counter not answer
            }
        } catch (Exception ex) {
            Logger.ErrToLog("*PowerOn" + ex.toString());
        }
        return result;
    }

    public static byte PoewerOff(byte addr1, byte addr2, byte addr3, byte addr4) {
        byte result = 2;
        try {
            Merc200 m = Merc200.SearchMerc200(addr1, addr2, addr3, addr4);
            if (m != null) {
                m.powerState = 2; //состояние реле нагрузки НЕ ОПРЕДЕЛЕНО
                byte[] answer = m.Cmd200(new byte[]{(byte) 0x71, (byte) 0xAA});
                if (answer[0] == 0x71) {
                    result = 0;
                    m.powerState = 0;  //состояние реле нагрузки ОТКЛ.
                } else {
                    result = 4;  //counter error
                }
            } else {
                result = 3;  //counter not answer
            }
        } catch (Exception ex) {
            Logger.ErrToLog("*PowerOff" + ex.toString());
        }
        return result;
    }

    public static byte[] GetState(byte addr1, byte addr2, byte addr3, byte addr4) {
        byte[] result = {2};  //2-counter not found
        try {
            Merc200 m = Merc200.SearchMerc200(addr1, addr2, addr3, addr4);
            if (m != null) {
                result = new byte[]{0, m.powerState};
            }
        } catch (Exception ex) {
            Logger.ErrToLog("*GetState" + ex.toString());
        }
        return result;
    }
}
