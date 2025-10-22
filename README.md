# Рудомётов Даниил Романович ИКС-433

### Всякое

События :

<img width="757" height="822" alt="изображение" src="https://github.com/user-attachments/assets/eef22cb9-7540-4eda-ac1e-241c8678c809" />

mediaplayer_library
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


