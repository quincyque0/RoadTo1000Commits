# Рудомётов Даниил Романович ИКС-433

### Полезное

События:

<img width="757" height="822" alt="изображение" src="https://github.com/user-attachments/assets/eef22cb9-7540-4eda-ac1e-241c8678c809" />

# mediaplayer_library


## Состояния
| Состояние             | Как попасть                      | Что можно делать       |
| --------------------- | -------------------------------- | ---------------------- |
| **Idle**              | `MediaPlayer()`                  | Только задать источник |
| **Initialized**       | `setDataSource()`                | Подготовка             |
| **Prepared**          | `prepare()` или `prepareAsync()` | Воспроизводить         |
| **Started**           | `start()`                        | Пауза, стоп, перемотка |
| **Paused**            | `pause()`                        | Возобновить `start()`  |
| **Stopped**           | `stop()`                         | Подготовить заново     |
| **PlaybackCompleted** | После конца трека                | `start()` снова        |
| **End**               | `release()`                      | Плеер уничтожен        |

prepare() — синхронная, блокирует поток 

prepareAsync() — готовит плеер в фоне; результат приходит в слушателе:

## Дополнительные методы
| Метод                                                                  | Назначение / пример                                                   |
| ---------------------------------------------------------------------- | --------------------------------------------------------------------- |
| `getDuration()`                                                        | Возвращает длину трека в миллисекундах                                |
| `getCurrentPosition()`                                                 | Возвращает текущую позицию воспроизведения                            |
| `isPlaying()`                                                          | Проверяет, идёт ли воспроизведение                                    |
| `setAudioStreamType(AudioManager.STREAM_MUSIC)`                        | Настраивает тип аудио потока (обычно для звонков, мультимедиа и т.д.) |
| `attachAuxEffect(int effectId)` / `setAuxEffectSendLevel(float level)` | Поддержка спец. эффектов (эхо, реверберация — если доступно)          |
| `setPlaybackParams(PlaybackParams)`                                    | Позволяет менять скорость и высоту тона (Android 6+)                  |

## Слушатели событий (Listeners)
| Слушатель                       | Назначение                                               |
| ------------------------------- | -------------------------------------------------------- |
| `setOnPreparedListener`         | Вызывается, когда плеер готов к воспроизведению          |
| `setOnCompletionListener`       | Когда трек дошёл до конца                                |
| `setOnErrorListener`            | При ошибках (например, файл не найден)                   |
| `setOnSeekCompleteListener`     | Когда перемотка завершена                                |
| `setOnBufferingUpdateListener`  | При обновлении буфера (для потоков)                      |
| `setOnInfoListener`             | Дополнительная информация (например, начало буферизации) |
| `setOnVideoSizeChangedListener` | Если используется для видео                              |

## Важно: 
Создавать обьекты нужно в папку app/res/raw/file.mp3

# Uri
URI (Uniform Resource Identifier) - это строка символов, которая идентифицирует ресурс. В контексте Android URI используется для идентификации различных типов данных и ресурсов.
стрктура: scheme:[//authority]path[?query][#fragment]

# Fragment
import androidx.fragment.app.Fragment


Fragment представляет собой повторно используемую часть пользовательского интерфейса вашего приложения. Фрагмент определяет и управляет собственным макетом, имеет собственный жизненный цикл и может обрабатывать собственные входные события. Фрагменты не могут жить сами по себе. Они должны размещаться в активности или другом фрагменте. Иерархия представлений фрагмента становится частью иерархии представлений хоста или присоединяется к ней .
<img width="447" height="507" alt="изображение" src="https://github.com/user-attachments/assets/1ea3f123-e6c2-4de9-b804-f3c575b9e72c" />



<img width="822" height="302" alt="изображение" src="https://github.com/user-attachments/assets/6d2be1e6-7dd0-404c-9d78-6fb12733583a" />

FragmentManager
Основные методы

    beginTransaction() - начало транзакции

    findFragmentById() - поиск по ID контейнера

    findFragmentByTag() - поиск по тегу

    getFragments() - список всех фрагментов

    popBackStack() - возврат по стеку

Back Stack методы

    getBackStackEntryCount() - количество записей

    popBackStackImmediate() - немедленный возврат

    addOnBackStackChangedListener() - слушатель изменений

 FragmentTransaction
Основные операции

    add() - добавление фрагмента

    replace() - замена фрагмента

    remove() - удаление фрагмента

    hide() / show() - скрытие/показ

Настройка транзакции

    addToBackStack() - добавление в стек

    setCustomAnimations() - анимации перехода

    setTransition() - тип перехода

Методы коммита

    commit() - асинхронный коммит

    commitNow() - синхронный коммит

    commitAllowingStateLoss() - коммит с потерей состояния


