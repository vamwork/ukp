package ukp;

import com.cinterion.io.ATCommand;
import java.util.Enumeration;

public class Task {

    //private boolean isJustStart = true;
    private boolean hourOfRestart = false;
    private int prevMinute = 0;
    private int prevHour = 1;
    public static final int period = 2000; // периодичность выполнения Task
    public static boolean isChannel = false;
    public Mercury master = null;
    public ATCommand atc;

    // рабочий "цикл" приложения
    public boolean RunTask() {
        boolean result = false;

        if (isChannel) {
            Logger.IamWork("X");
        } else {
            Logger.IamWork(".");
        }

        try {
            DateTime dt = DtService.GetDateTime(true);

            if (dt == null) {
                if (Service.msgList.size() == 0) {
                    //время не синхронизировано, а очередь сообщений пуста
                    Event.AddEvent((byte) 1, "001");  //только что запустился
                    Service.OrderMultipleProcessing();
                }
            }

            if (!isChannel) {
                if (Controller.timeIsSynchro) {
                    if ((dt.hour != prevHour) && (dt.minute > 1)) {
                        prevHour = dt.hour;
                        hourOfRestart = false;

                        if (prevHour == 0) {
                            //наступили новые сутки                            
                            Merc200.SetEnergyReadCounter();
                        }

                        if (prevHour == 1) {
                            // Mercury.SetSheduleEnergyReadCounter();
                            Mercury.SetDayEnergyReadCounter();
                            Mercury.SetMonthEnergyReadCounter();
                            Mercury.SetSheduleEnergyReadCounter();
                            Mercury.SetSheduleTimeCorrect();
                        }

                        // удаление старых архивов
                        if (prevHour == 3) {
                            Archiver.profile.GarbageCollector();
                            Archiver.energy.GarbageCollector();
                        }
                    }
                }

//                if (isJustStart) {
//                    //только что запустились
//
//                    //TODO
//                    Logger.AddToLog("isJustStart");
//
//                    isJustStart = false;
//                    Logger.AddToLog("isJustStart-ups");
//                    Event.AddEvent((byte) 1, "001");
//                    Logger.AddToLog("isJustStart-0");
//                    Event.SaveEvents();
//                    Logger.AddToLog("isJustStart-1");
//                    Mercury.PreparePowerProfiles();
//                    Logger.AddToLog("isJustStart-2");
//                    Mercury.SetDayEnergyReadCounter();
//                    Logger.AddToLog("isJustStart-3");
//                    Mercury.SetMonthEnergyReadCounter();
//                    Logger.AddToLog("isJustStart-4");
//                    Mercury.SetSheduleEnergyReadCounter();
//                    Logger.AddToLog("isJustStart-5");
//                    Mercury.SetSheduleTimeCorrect();
//                    Logger.AddToLog("isJustStart-6");
//                    Mercury.SetSheduleEvents();
//                    Logger.AddToLog("isJustStart-7");
//
//                    //TODO
//                    Logger.AddToLog("isJustStart - END");
//                    //сюда мы не дошли                    
//                }
                if (!isChannel) {
                    Service.OrderMultipleProcessing();
                }

                if (Controller.timeIsSynchro) {
                    // проверим дозвон

                    if (Dialer.tryCounter > 0) {

                        if (Dialer.toNextDialingWait > 0) {
                            Dialer.toNextDialingWait--;
                        } else {
                            Dialer.Dial();
                        }
                    }

                    // проверка новой минуты
                    if (dt.minute != prevMinute) {
                        prevMinute = dt.minute;

                        Mercury.SetSheduleTimeCorrect();

                        if ((prevMinute % 30) == 0) {
                            // наступила очередная получасовка - установим попытки опроса                            
                            Mercury.SetReadCounter();
                            Mercury.SetSheduleAddtional();

                            Merc200.SetPowerReadCounter();
                            Merc200.SetProfileReadCounter();
                        }

                        if (!isChannel) {
                            if ((prevMinute % 5) == 0) {
                                Mercury.SborIU(); // проведем очередной опрос счётчиков для сбора U и I
                                Mercury.SetSheduleEvents();
                            }

                            if ((prevMinute % 7) == 0) {
                                Mercury.PreparePowerProfiles(); // обновить профиль мощности
                            }

                            if ((prevMinute % 8) == 0) {
                                Mercury.ReadPowerProfiles(); // опросить счетчики                                
                                Mercury.GenerateMsgByPowerProfiles(); // запуск цикла отправки профиля мощности                                
                            }

                            if ((prevMinute % 20) == 0) {
                                Controller.cp.ReOpenComPort();
                            }
                        }
                    }

                    // ведем счётчик открытости канала
                    if (Controller.channelIsOpen >= 0) {
                        Controller.channelIsOpen = Controller.channelIsOpen - period;
                        if (Controller.channelIsOpen < 0) {
                            // закрытие канала
                            Controller.channelIsOpen = 0;
                        }
                    }

                    // если есть канал, то обнулим попытки дозвона
                    if (isChannel & (Dialer.tryCounter > 0)) {
                        Dialer.tryCounter = 0; // сбросим сразу в ноль
                    }

                    // проверим наличие GSM-канала
                    if (!isChannel) {
                        // попытка сбора
                        try {
                            Mercury.SborExecute();
                            Merc200.SborExecute();
                        } catch (Exception e) {
                            Logger.ErrToLog("*Task[0001]" + e.toString());
                        }
                        // опрос дискретных модулей                       
                        AnJournal.Save(); // запись, при необходимости, журнала аналоговых событий                                                
                    }
                } else {
                    Logger.IamWork("NO_TIME");
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Task[0000]" + e.toString());
        }
        return result;
    }
}
