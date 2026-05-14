package com.hivetrack.mobile

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.hivetrack.mobile.network.ApiClient
import com.hivetrack.mobile.network.ApiException
import com.hivetrack.mobile.session.UserSession
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var session: UserSession
    private lateinit var root: LinearLayout
    private lateinit var output: TextView

    private val bg = Color.rgb(249, 246, 239)
    private val surface = Color.WHITE
    private val ink = Color.rgb(43, 38, 30)
    private val muted = Color.rgb(116, 109, 99)
    private val line = Color.rgb(233, 226, 214)
    private val accent = Color.rgb(177, 124, 45)
    private val accentDark = Color.rgb(125, 82, 25)
    private val accentSoft = Color.rgb(249, 236, 203)
    private val good = Color.rgb(37, 132, 91)
    private val danger = Color.rgb(191, 63, 63)
    private val dark = Color.rgb(48, 44, 36)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = UserSession(this)
        if (session.isLoggedIn()) showHome() else showLogin()
    }

    private fun screen(title: String, subtitle: String? = null, showBack: Boolean = false) {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(24))
        }
        scroll.addView(root)
        setContentView(scroll)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(14))
        }
        root.addView(header, matchWrap())

        if (showBack) {
            header.addView(iconButton("‹") { showHome() }, ViewGroup.LayoutParams(dp(42), dp(42)))
        } else {
            header.addView(hexLogo(dp(42)), ViewGroup.LayoutParams(dp(42), dp(42)))
        }
        addSpace(header, 10, true)

        val titles = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        header.addView(titles, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        titles.addView(TextView(this).apply {
            text = title
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
        })
        if (subtitle != null) {
            titles.addView(TextView(this).apply {
                text = subtitle
                textSize = 13f
                setTextColor(muted)
                setPadding(0, dp(2), 0, 0)
            })
        }

        if (session.isLoggedIn() && !showBack) {
            header.addView(TextView(this).apply {
                text = initials(session.fullName.ifBlank { session.email })
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
                textSize = 14f
                setTextColor(Color.WHITE)
                background = round(accent, 16f)
            }, ViewGroup.LayoutParams(dp(42), dp(42)))
        }
    }

    private fun showLogin() {
        screen("HiveTrack", "мобільний кабінет автошколи")

        card(dark, border = 0, padding = 22) {
            addView(TextView(this@MainActivity).apply {
                text = "⬢"
                textSize = 34f
                setTextColor(accent)
            })
            addView(TextView(this@MainActivity).apply {
                text = "Контроль навчання, розкладу та практики"
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                setPadding(0, dp(8), 0, dp(6))
            })
            addView(TextView(this@MainActivity).apply {
                text = "Увійдіть у систему, щоб навчатись."
                textSize = 14f
                setTextColor(Color.rgb(222, 216, 205))
            })
        }

        val email = input("Email", "newadmin@test.com")
        val password = input("Пароль", "123456", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

        button("Увійти", true) {
            val e = email.text.toString().trim()
            val p = password.text.toString().trim()
            if (e.isBlank() || p.isBlank()) toast("Введи email і пароль") else login(e, p)
        }

        smallText("Тестовий вхід: newadmin@test.com / 123456")

        card(padding = 16) {
            sectionTitle("Швидка реєстрація")
            val fullName = inputInline(this, "Повне ім’я", "Mobile Student")
            val regEmail = inputInline(this, "Email", "mobilestudent@test.com")
            val regPassword = inputInline(this, "Пароль", "123456")
            val roleSpinner = Spinner(this@MainActivity).apply {
                adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Student", "Instructor", "Admin"))
                background = round(Color.rgb(254, 252, 248), 12f, line)
            }
            addView(roleSpinner, matchWrap(mTop = 10))
            addView(styledButton("Зареєструвати", false) {
                register(fullName.text.toString(), regEmail.text.toString(), regPassword.text.toString(), roleSpinner.selectedItem.toString())
            }, matchWrap(mTop = 12))
        }

        addOutput("Результати запитів будуть показані тут.")
    }

    private fun login(email: String, password: String) {
        setLoading("Виконую вхід...")
        runApi {
            val raw = ApiClient.post("/api/auth/login", JSONObject().put("email", email).put("password", password))
            val json = JSONObject(raw)
            val user = json.getJSONObject("user")
            session.token = json.getString("token")
            session.userId = user.getInt("id")
            session.fullName = user.optString("fullName")
            session.email = user.optString("email")
            session.role = user.optString("role")
            ui { showHome() }
        }
    }

    private fun register(fullName: String, email: String, password: String, role: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            toast("Заповни дані для реєстрації")
            return
        }
        setLoading("Створюю користувача...")
        runApi {
            val body = JSONObject()
                .put("fullName", fullName)
                .put("email", email)
                .put("password", password)
                .put("role", role)
            val raw = ApiClient.post("/api/auth/register", body)
            ui { renderCreatedUser(JSONObject(raw)) }
        }
    }

    private fun showHome() {
        screen("Привіт, ${firstName()}", "${roleUk(session.role)} · HiveTrack Mobile")
        roleHeader()

        when (session.role) {
            "Student" -> studentDashboard()
            "Instructor" -> instructorDashboard()
            "Admin" -> adminDashboard()
            else -> {
                addOutput("Невідома роль користувача: ${session.role}")
                button("Вийти") { session.clear(); showLogin() }
            }
        }
    }

    private fun roleHeader() {
        card(dark, border = 0, padding = 18) {
            addView(tag(roleUk(session.role), Color.WHITE, accent))
            addView(TextView(this@MainActivity).apply {
                text = when (session.role) {
                    "Student" -> "Особистий кабінет учня"
                    "Instructor" -> "Кабінет інструктора"
                    "Admin" -> "Адміністративна панель"
                    else -> "Користувач HiveTrack"
                }
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                setPadding(0, dp(10), 0, dp(4))
            })
            addView(TextView(this@MainActivity).apply {
                text = "${session.fullName}\n${session.email} · ID ${session.userId}"
                textSize = 13f
                setTextColor(Color.rgb(224, 217, 205))
            })
        }
    }

    private fun studentDashboard() {
        sectionTitle("Доступно учню")
        actionGrid(listOf(
            Action("Огляд", "Мої показники", "▤") { loadStudentDashboard() },
            Action("Моя група", "Призначена група", "▦") { loadMyGroup() },
            Action("Моя відвідуваність", "Історія занять", "◷") { loadMyAttendance() },
            Action("Мої тести", "ПДР і результати", "?" ) { loadMyTests() },
            Action("Сповіщення", "Важливі події", "!") { loadNotifications() },
            Action("Профіль", "Особисті дані", "◉") { loadMe() }
        ))
        button("Вийти") { session.clear(); showLogin() }
        studentQuickOverview()
    }

    private fun instructorDashboard() {
        sectionTitle("Доступно інструктору")
        actionGrid(listOf(
            Action("Огляд", "Сьогодні й учні", "▤") { loadInstructorDashboard() },
            Action("Мої заняття", "Тільки мої заняття", "◷") { loadInstructorLessons() },
            Action("Мої учні", "Групи інструктора", "▦") { loadInstructorStudents() },
            Action("Створити заняття", "Форма заняття", "+") { showCreateLessonForm(false) },
            Action("Відвідуваність", "Додати запис", "✓") { showCreateAttendanceForm() },
            Action("Сповіщення", "Особисті події", "!") { loadNotifications() },
            Action("IoT", "Сесії водіння", "⬡") { showIot() },
            Action("Профіль", "Дані акаунта", "◉") { loadMe() }
        ))
        button("Вийти") { session.clear(); showLogin() }
        instructorQuickOverview()
    }

    private fun adminDashboard() {
        sectionTitle("Адміністрування")
        actionGrid(listOf(
            Action("Огляд", "Dashboard системи", "▤") { loadAdminDashboard() },
            Action("Статистика", "Користувачі й записи", "▤") { loadAdminStats() },
            Action("Групи", "Усі групи системи", "▦") { showAllGroups() },
            Action("Зарахування", "Учні у групах", "⇄") { showEnrollments() }
        ))
        actionGrid(listOf(
            Action("Розклад", "Усі заняття", "◷") { showAllLessons() },
            Action("Нова група", "Створити групу", "+") { showCreateGroupForm() },
            Action("Нове заняття", "Для інструктора", "+") { showCreateLessonForm(true) },
            Action("Відвідуваність", "Створити запис", "✓") { showCreateAttendanceForm() }
        ))
        actionGrid(listOf(
            Action("Сповіщення", "Створити/переглянути", "!") { showNotificationsAdmin() },
            Action("Аудит", "Журнал дій", "☰") { loadAuditLogs() },
            Action("IoT", "Телеметрія", "⬡") { showIot() },
            Action("Профіль", "Дані адміністратора", "◉") { loadMe() }
        ))
        button("Вийти") { session.clear(); showLogin() }
        adminQuickOverview()
    }


    private fun studentQuickOverview() {
        card(padding = 16) {
            sectionTitle("Коротко")
            addView(TextView(this@MainActivity).apply {
                text = "Тут показані тільки твої персональні дані: група, історія відвідуваності та тести ПДР. Інші групи й чужі записи учню недоступні."
                textSize = 14f
                setTextColor(muted)
            })
        }
    }

    private fun instructorQuickOverview() {
        card(padding = 16) {
            sectionTitle("Коротко")
            addView(TextView(this@MainActivity).apply {
                text = "Інструктор працює зі своїми заняттями, може створювати заняття, відмічати відвідуваність учнів і передавати IoT-дані."
                textSize = 14f
                setTextColor(muted)
            })
        }
    }

    private fun adminQuickOverview() {
        card(padding = 16) {
            sectionTitle("Коротко")
            addView(TextView(this@MainActivity).apply {
                text = "Адміністратор керує групами, розкладом, відвідуваністю, статистикою та IoT-сесіями."
                textSize = 14f
                setTextColor(muted)
            })
        }
    }


    private fun loadStudentDashboard() {
        screen("Огляд учня", "Особисті показники навчання", true)
        addOutput("Завантаження огляду...")
        runApi {
            val json = JSONObject(ApiClient.get("/api/student/me/dashboard", session.token))
            ui {
                root.removeViewSafe(output)
                val group = json.optJSONObject("group")
                val attendance = json.optJSONObject("attendance") ?: JSONObject()
                val tests = json.optJSONObject("tests") ?: JSONObject()
                val next = json.optJSONObject("nextLesson")
                dashboardMetric("Моя група", group?.optString("name") ?: "Не призначено", "Категорія: ${group?.optString("category") ?: "-"}")
                dashboardMetric("Відвідуваність", "${attendance.optInt("rate", 0)}%", "Присутній: ${attendance.optInt("present", 0)} із ${attendance.optInt("total", 0)}")
                dashboardMetric("Тести ПДР", tests.optString("averageScore", "-") + "%", "Завершено тестів: ${tests.optInt("completed", 0)}")
                dashboardMetric("Найближче заняття", next?.optString("topic") ?: "Поки немає", "Дата: ${formatDate(next?.optString("lessonDateTime") ?: "")}")
                dashboardMetric("Сповіщення", json.optInt("unreadNotifications", 0).toString(), "Непрочитані повідомлення")
            }
        }
    }

    private fun loadInstructorDashboard() {
        screen("Огляд інструктора", "Заняття, учні та IoT", true)
        addOutput("Завантаження огляду...")
        runApi {
            val json = JSONObject(ApiClient.get("/api/instructor/me/dashboard", session.token))
            ui {
                root.removeViewSafe(output)
                dashboardMetric("Занять сьогодні", json.optInt("lessonsToday").toString(), "Поточний день")
                dashboardMetric("Мої учні", json.optInt("studentsCount").toString(), "Активні учні у групах")
                dashboardMetric("Відміток сьогодні", json.optInt("attendanceToday").toString(), "Записи відвідуваності")
                dashboardMetric("IoT-сесії", json.optInt("iotSessions").toString(), "Передані сесії водіння")
                dashboardMetric("Сповіщення", json.optInt("unreadNotifications").toString(), "Непрочитані повідомлення")
            }
        }
    }

    private fun loadAdminDashboard() {
        screen("Огляд адміністратора", "Загальна картина системи", true)
        addOutput("Завантаження dashboard...")
        runApi {
            val json = JSONObject(ApiClient.get("/api/admin/dashboard", session.token))
            ui {
                root.removeViewSafe(output)
                dashboardMetric("Користувачі", json.optInt("users").toString(), "Активні акаунти")
                dashboardMetric("Групи", json.optInt("groups").toString(), "Навчальні групи")
                dashboardMetric("Заняття", json.optInt("lessons").toString(), "У розкладі")
                dashboardMetric("Відвідуваність", json.optInt("attendanceRecords").toString(), "Записів журналу")
                dashboardMetric("Тести", json.optInt("testAttempts").toString(), "Спроб проходження")
                dashboardMetric("Аудит", json.optInt("auditEvents").toString(), "Подій системи")
            }
        }
    }

    private fun dashboardMetric(title: String, value: String, subtitle: String) {
        dataCard(title = title, subtitle = subtitle, badge = value)
    }

    private fun loadNotifications() {
        screen("Сповіщення", "Особисті повідомлення", true)
        addOutput("Завантаження сповіщень...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/notifications/my", session.token))
            ui { renderNotifications(arr) }
        }
    }

    private fun renderNotifications(arr: JSONArray) {
        root.removeViewSafe(output)
        if (arr.length() == 0) {
            emptyState("Сповіщень поки немає", "Коли в системі з’являться важливі події, вони будуть показані тут.")
            return
        }
        for (i in 0 until arr.length()) {
            val n = arr.getJSONObject(i)
            val isRead = n.optInt("isRead", 0) == 1
            dataCard(
                title = n.optString("title", "Сповіщення"),
                subtitle = "${n.optString("message", "-")}\nДата: ${formatDate(n.optString("createdAt"))}",
                badge = if (isRead) "Прочитано" else "Нове"
            ) {
                if (!isRead) markNotificationRead(n.optInt("id")) else toast("Сповіщення вже прочитано")
            }
        }
    }

    private fun markNotificationRead(id: Int) {
        setLoading("Позначаю як прочитане...")
        runApi {
            ApiClient.put("/api/notifications/$id/read", JSONObject(), session.token)
            ui { loadNotifications() }
        }
    }

    private fun showNotificationsAdmin() {
        screen("Сповіщення", "Адміністратор", true)
        button("Мої сповіщення") { loadNotifications() }
        button("Створити сповіщення", true) { showCreateNotificationForm() }
        addOutput("Адміністратор може надіслати повідомлення конкретному користувачу за ID.")
    }

    private fun showCreateNotificationForm() {
        screen("Нове сповіщення", "Надіслати користувачу", true)
        val userId = input("ID користувача", "1", InputType.TYPE_CLASS_NUMBER)
        val title = input("Заголовок", "Оновлення розкладу")
        val message = input("Текст", "Перевірте актуальний розклад занять у застосунку")
        button("Надіслати", true) {
            setLoading("Надсилаю сповіщення...")
            runApi {
                val body = JSONObject()
                    .put("userId", userId.text.toString().toInt())
                    .put("title", title.text.toString())
                    .put("message", message.text.toString())
                val raw = ApiClient.post("/api/admin/notifications", body, session.token)
                ui {
                    root.removeViewSafe(output)
                    resultHeader("Сповіщення створено", "Користувач побачить його у своєму кабінеті")
                    val json = JSONObject(raw)
                    dataCard(json.optString("title"), json.optString("message"), "ID ${json.optInt("id")}")
                }
            }
        }
        addOutput()
    }

    private fun showEnrollments() {
        screen("Зарахування", "Учні у групах", true)
        button("Створити зарахування", true) { showCreateEnrollmentForm() }
        button("Оновити список") { loadEnrollments() }
        addOutput("Завантаження...")
        loadEnrollments()
    }

    private fun loadEnrollments() {
        runApi {
            val arr = JSONArray(ApiClient.get("/api/admin/enrollments", session.token))
            ui { renderEnrollments(arr) }
        }
    }

    private fun renderEnrollments(arr: JSONArray) {
        root.removeViewSafe(output)
        if (arr.length() == 0) {
            emptyState("Зарахувань поки немає", "Додай учня до групи через кнопку створення зарахування.")
            return
        }
        for (i in 0 until arr.length()) {
            val e = arr.getJSONObject(i)
            dataCard(
                title = e.optString("studentName", "Учень"),
                subtitle = "Email: ${e.optString("email", "-")}\nГрупа: ${e.optString("groupName", "-")}\nСтатус: ${e.optString("status", "-")}",
                badge = "ID ${e.optInt("id")}"
            )
        }
    }

    private fun showCreateEnrollmentForm() {
        screen("Додати учня до групи", "Адміністрування", true)
        val studentId = input("ID учня", "1", InputType.TYPE_CLASS_NUMBER)
        val groupId = input("ID групи", "1", InputType.TYPE_CLASS_NUMBER)
        button("Зарахувати", true) {
            setLoading("Створюю зарахування...")
            runApi {
                val body = JSONObject()
                    .put("studentId", studentId.text.toString().toInt())
                    .put("groupId", groupId.text.toString().toInt())
                    .put("status", "Active")
                val raw = ApiClient.post("/api/admin/enrollments", body, session.token)
                ui {
                    root.removeViewSafe(output)
                    resultHeader("Учня додано до групи", "Зарахування збережено у системі")
                    val json = JSONObject(raw)
                    dataCard("Учень ID ${json.optInt("studentId")}", "Група ID: ${json.optInt("groupId")}\nСтатус: ${json.optString("status")}", "ID ${json.optInt("id")}")
                }
            }
        }
        addOutput()
    }

    private fun loadInstructorStudents() {
        screen("Мої учні", "Учні з груп інструктора", true)
        addOutput("Завантаження учнів...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/instructor/me/students", session.token))
            ui {
                root.removeViewSafe(output)
                if (arr.length() == 0) {
                    emptyState("Учнів поки немає", "Учні з’являться після зарахування до груп інструктора.")
                } else {
                    for (i in 0 until arr.length()) {
                        val u = arr.getJSONObject(i)
                        dataCard(u.optString("fullName", "Учень"), "Email: ${u.optString("email", "-")}\nГрупа: ${u.optString("groupName", "-")}", "ID ${u.optInt("id")}")
                    }
                }
            }
        }
    }

    private fun loadAuditLogs() {
        screen("Журнал дій", "Системний аудит", true)
        addOutput("Завантаження журналу...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/admin/audit", session.token))
            ui { renderAuditLogs(arr) }
        }
    }

    private fun renderAuditLogs(arr: JSONArray) {
        root.removeViewSafe(output)
        if (arr.length() == 0) {
            emptyState("Журнал дій порожній", "Після створення груп, занять або відвідуваності тут з’являться події.")
            return
        }
        for (i in 0 until arr.length()) {
            val a = arr.getJSONObject(i)
            dataCard(
                title = actionUk(a.optString("action", "Подія")),
                subtitle = "Користувач: ${a.optString("userName", "Система")}\nДата: ${formatDate(a.optString("createdAt"))}\nДеталі: ${a.optString("details", "-")}",
                badge = "ID ${a.optInt("id")}"
            )
        }
    }

    private fun showAllGroups() {
        screen("Групи", "Адміністратор: усі групи", true)
        addOutput("Завантаження груп...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/groups"))
            ui { renderGroups(arr, onlyMine = false) }
        }
    }

    private fun loadMyGroup() {
        screen("Моя група", "Група, до якої призначено учня", true)
        addOutput("Завантаження...")
        runApi {
            val json = JSONObject(ApiClient.get("/api/student/me/group", session.token))
            ui {
                root.removeViewSafe(output)
                if (json.isNull("group")) {
                    emptyState("Групу ще не призначено", "Адміністратор має додати учня до групи. Для нових учнів це виконується автоматично під час реєстрації, якщо в системі вже є група.")
                } else {
                    val g = json.getJSONObject("group")
                    dataCard(
                        title = g.optString("name", "Моя група"),
                        subtitle = "Категорія: ${g.optString("category", "-")}\nПочаток: ${g.optString("startDate", "-")} · Завершення: ${g.optString("endDate", "-")}",
                        badge = "Моя група"
                    ) { loadLessonsByGroup(g.optInt("id"), true) }
                }
            }
        }
    }

    private fun renderGroups(arr: JSONArray, onlyMine: Boolean) {
        root.removeViewSafe(output)
        if (arr.length() == 0) {
            emptyState("Груп поки немає", "Створи групу через адміністративний кабінет.")
            return
        }
        for (i in 0 until arr.length()) {
            val g = arr.getJSONObject(i)
            dataCard(
                title = g.optString("name", "Група"),
                subtitle = "Категорія: ${g.optString("category", "-")}\nПочаток: ${g.optString("startDate", "-")} · Завершення: ${g.optString("endDate", "-")}",
                badge = if (onlyMine) "Моя" else "ID ${g.optInt("id")}" 
            ) { loadLessonsByGroup(g.optInt("id"), onlyMine) }
        }
    }

    private fun loadLessonsByGroup(groupId: Int, studentMode: Boolean = false) {
        screen(if (studentMode) "Заняття моєї групи" else "Заняття групи", "Група ID $groupId", true)
        addOutput("Завантаження занять...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/lessons/group/$groupId"))
            ui { renderLessons(arr, studentMode = studentMode) }
        }
    }

    private fun showAllLessons() {
        screen("Розклад", "Адміністратор: усі заняття", true)
        addOutput("Завантаження розкладу...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/lessons"))
            ui { renderLessons(arr, studentMode = false) }
        }
    }

    private fun loadInstructorLessons() {
        screen("Мої заняття", "Інструктор ID ${session.userId}", true)
        addOutput("Завантаження занять...")
        runApi {
            val mine = JSONArray(ApiClient.get("/api/instructor/me/lessons", session.token))
            ui { renderLessons(mine, studentMode = false, instructorMode = true) }
        }
    }

    private fun renderLessons(arr: JSONArray, studentMode: Boolean = false, instructorMode: Boolean = false) {
        root.removeViewSafe(output)
        if (arr.length() == 0) {
            emptyState("Занять поки немає", "Для цієї ролі або групи в базі ще немає занять.")
            return
        }
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            val lessonId = item.optInt("id")
            val subtitle = buildString {
                append("Тип: ${lessonTypeUk(item.optString("type"))}\n")
                append("Дата: ${formatDate(item.optString("lessonDateTime"))}\n")
                append("Група: ${item.optString("groupName", item.optString("groupId", "-"))}\n")
                append("Інструктор: ${item.optString("instructorName", item.optString("instructorId", "-"))}")
            }
            dataCard(
                title = item.optString("topic", "Заняття"),
                subtitle = subtitle,
                badge = "ID $lessonId",
                onClick = if (studentMode) null else ({ loadAttendanceByLesson(lessonId) })
            )
        }
    }

    private fun loadMyTests() {
        screen("Мої тести", "Тести ПДР та результати", true)
        addOutput("Завантаження тестів...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/student/me/tests", session.token))
            ui { renderStudentTests(arr) }
        }
    }

    private fun renderStudentTests(arr: JSONArray) {
        root.removeViewSafe(output)
        if (arr.length() == 0) {
            emptyState("Тестів поки немає", "Адміністратор ще не додав тести ПДР у систему.")
            return
        }
        for (i in 0 until arr.length()) {
            val t = arr.getJSONObject(i)
            val status = t.optString("status", "Not started")
            val scoreText = if (t.isNull("score")) "ще немає" else "${t.optInt("score")}%"
            dataCard(
                title = t.optString("name", "Тест ПДР"),
                subtitle = "Тема: ${t.optString("topic", "-")}\nРежим: ${testModeUk(t.optString("mode"))} · Ліміт: ${t.optInt("timeLimitMinutes", 20)} хв\nСтатус: ${testStatusUk(status)} · Результат: $scoreText\nНатисни, щоб створити демонстраційну спробу",
                badge = testModeUk(t.optString("mode"))
            ) { startTestAttempt(t.optInt("id")) }        }
    }

    private fun startTestAttempt(testId: Int) {
        setLoading("Зберігаю результат тесту...")
        runApi {
            val raw = ApiClient.post("/api/student/me/tests/$testId/attempt", JSONObject(), session.token)
            val json = JSONObject(raw)
            ui {
                root.removeViewSafe(output)
                resultHeader("Тест завершено", "Результат успішно збережено")
                dataCard(
                    title = json.optString("testName", "Тест ПДР"),
                    subtitle = "Результат: ${json.optInt("score")}%\nСтатус: ${testStatusUk(json.optString("status"))}",
                    badge = "ID ${json.optInt("id")}"
                ) { loadMyTests() }
            }
        }
    }

    private fun showCreateGroupForm() {
        screen("Нова група", "Адміністрування", true)
        val name = input("Назва групи", "Група B-3")
        val category = input("Категорія", "B")
        val startDate = input("Дата початку", "2026-03-01")
        val endDate = input("Дата завершення", "2026-06-01")
        button("Створити групу", true) {
            setLoading("Створюю групу...")
            runApi {
                val body = JSONObject()
                    .put("name", name.text.toString())
                    .put("category", category.text.toString())
                    .put("startDate", startDate.text.toString())
                    .put("endDate", endDate.text.toString())
                val raw = ApiClient.post("/api/groups", body, session.token)
                ui { renderCreatedGroup(JSONObject(raw)) }
            }
        }
        addOutput()
    }

    private fun showCreateLessonForm(adminMode: Boolean) {
        screen("Нове заняття", if (adminMode) "Адміністратор" else "Інструктор", true)
        val groupId = input("ID групи", "1", InputType.TYPE_CLASS_NUMBER)
        val instructorId = if (adminMode) input("ID інструктора", session.userId.toString(), InputType.TYPE_CLASS_NUMBER) else null
        if (!adminMode) smallText("ID інструктора буде автоматично взято з поточної сесії: ${session.userId}")
        val typeSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Theory", "Practice"))
            background = round(Color.rgb(254, 252, 248), 12f, line)
        }
        root.addView(typeSpinner, matchWrap(mTop = 8, mBottom = 8))
        val topic = input("Тема", "Практичне водіння у місті")
        val date = input("Дата і час", "2026-03-10 16:00:00")
        button("Створити заняття", true) {
            setLoading("Створюю заняття...")
            runApi {
                val body = JSONObject()
                    .put("groupId", groupId.text.toString().toInt())
                    .put("instructorId", instructorId?.text?.toString()?.toInt() ?: session.userId)
                    .put("type", typeSpinner.selectedItem.toString())
                    .put("topic", topic.text.toString())
                    .put("lessonDateTime", date.text.toString())
                val raw = ApiClient.post("/api/lessons", body, session.token)
                ui { renderCreatedLesson(JSONObject(raw)) }
            }
        }
        addOutput()
    }

    private fun showCreateAttendanceForm() {
        screen("Відвідуваність", "Додати або оновити запис", true)
        val lessonId = input("ID заняття", "1", InputType.TYPE_CLASS_NUMBER)
        val studentId = input("ID учня", "1", InputType.TYPE_CLASS_NUMBER)
        val statusSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Present", "Absent", "Late"))
            background = round(Color.rgb(254, 252, 248), 12f, line)
        }
        root.addView(statusSpinner, matchWrap(mTop = 8, mBottom = 8))
        val comment = input("Коментар", "Запис створено з мобільного застосунку")
        button("Зберегти", true) {
            setLoading("Зберігаю...")
            runApi {
                val body = JSONObject()
                    .put("lessonId", lessonId.text.toString().toInt())
                    .put("studentId", studentId.text.toString().toInt())
                    .put("status", statusSpinner.selectedItem.toString())
                    .put("comment", comment.text.toString())
                val raw = ApiClient.post("/api/attendance", body, session.token)
                ui { renderCreatedAttendance(JSONObject(raw)) }
            }
        }
        addOutput()
    }

    private fun loadAttendanceByLesson(lessonId: Int) {
        screen("Відвідуваність", "Заняття ID $lessonId", true)
        addOutput("Завантаження...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/attendance/lesson/$lessonId"))
            ui { renderAttendanceByLesson(arr) }
        }
    }

    private fun renderAttendanceByLesson(arr: JSONArray) {
        root.removeViewSafe(output)
        if (arr.length() == 0) {
            emptyState("Записів відвідуваності немає", "Для цього заняття ще не створено жодного запису.")
            return
        }
        for (i in 0 until arr.length()) {
            val a = arr.getJSONObject(i)
            dataCard(
                title = a.optString("studentName", "Учень ID ${a.optInt("studentId")}"),
                subtitle = "Статус: ${statusUk(a.optString("status"))}\nКоментар: ${a.optString("comment", "-")}",
                badge = "ID ${a.optInt("id")}" 
            )
        }
    }


    private fun loadMyAttendance() {
        screen("Моя відвідуваність", "Історія занять учня", true)
        addOutput("Завантаження...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/student/me/attendance", session.token))
            ui { renderStudentAttendance(arr) }
        }
    }

    private fun loadStudentAttendance(studentId: Int) {
        screen("Моя відвідуваність", "Історія занять учня", true)
        addOutput("Завантаження...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/attendance/student/$studentId"))
            ui { renderStudentAttendance(arr) }
        }
    }

    private fun renderStudentAttendance(arr: JSONArray) {
        root.removeViewSafe(output)
        if (arr.length() == 0) {
            emptyState("Історія відвідуваності порожня", "Інструктор або адміністратор ще не додав відвідуваність для цього учня.")
            return
        }
        for (i in 0 until arr.length()) {
            val a = arr.getJSONObject(i)
            dataCard(
                title = a.optString("lessonTopic", "Заняття"),
                subtitle = buildString {
                    append("Дата: ${formatDate(a.optString("lessonDateTime"))}\n")
                    append("Статус: ${statusUk(a.optString("status"))}\n")
                    if (a.has("groupName")) append("Група: ${a.optString("groupName")}\n")
                    append("Коментар: ${a.optString("comment", "-")}")
                },
                badge = "ID ${a.optInt("lessonId")}" 
            )
        }
    }

    private fun loadMe() {
        screen("Профіль", "Дані поточного користувача", true)
        card(padding = 18) {
            addView(TextView(this@MainActivity).apply {
                text = session.fullName.ifBlank { "Користувач" }
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(ink)
            })
            addView(TextView(this@MainActivity).apply {
                text = "Email: ${session.email}\nРоль: ${roleUk(session.role)}\nID: ${session.userId}"
                textSize = 14f
                setTextColor(muted)
                setPadding(0, dp(6), 0, 0)
            })
        }
        
    }

    private fun loadAdminStats() {
        screen("Статистика", "Адміністративний огляд", true)
        addOutput("Завантаження...")
        runApi {
            val raw = ApiClient.get("/api/admin/statistics/users", session.token)
            val json = JSONObject(raw)
            ui {
                root.removeViewSafe(output)
                card(padding = 16) {
                    sectionTitle("Користувачі")
                    addView(TextView(this@MainActivity).apply {
                        text = "Усього: ${json.optInt("totalUsers")}\nУчнів: ${json.optInt("studentsCount")}\nІнструкторів: ${json.optInt("instructorsCount")}\nАдміністраторів: ${json.optInt("adminsCount")}\nЗаблоковано: ${json.optInt("blockedUsers")}"
                        textSize = 16f
                        setTextColor(ink)
                    })
                }
                card(padding = 16) {
                    sectionTitle("Дані системи")
                    addView(TextView(this@MainActivity).apply {
                        text = "Груп: ${json.optInt("groupsCount")}\nЗанять: ${json.optInt("lessonsCount")}\nЗаписів відвідуваності: ${json.optInt("attendanceRecordsCount")}\nIoT-сесій: ${json.optInt("iotSessionsCount")}"
                        textSize = 16f
                        setTextColor(ink)
                    })
                }
            }
        }
    }

    private fun showIot() {
        screen("IoT-сесії", "SmartDriveTracker", true)
        button("Відправити тестову сесію", true) { sendIotSession() }
        button("Переглянути IoT-сесії") { loadIotSessions() }
        addOutput("IoT-модуль демонструє передачу телеметрії водіння на сервер.")
    }

    private fun sendIotSession() {
        setLoading("Відправляю тестову IoT-сесію...")
        runApi {
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
            val report = JSONObject()
                .put("studentId", session.userId.toString())
                .put("instructorId", session.userId.toString())
                .put("groupId", "1")
                .put("carId", "CAR-MOBILE-001")
                .put("startedAt", now)
                .put("finishedAt", now)
                .put("durationSec", 1800)
                .put("avgSpeed", 42.5)
                .put("hardBrakesTotal", 1)
                .put("laneViolationsTotal", 0)
                .put("overspeedEventsTotal", 2)
                .put("score", 89)
                .put("category", "Good")
            val raw = ApiClient.post("/api/iot/sessions", report)
            ui { renderIotCreated(JSONObject(raw)) }
        }
    }

    private fun loadIotSessions() {
        setLoading("Завантажую IoT-сесії...")
        runApi {
            val arr = JSONArray(ApiClient.get("/api/iot/sessions"))
            ui { renderIotSessions(arr) }
        }
    }

    private fun renderIotSessions(arr: JSONArray) {
        root.removeViewSafe(output)
        if (arr.length() == 0) {
            emptyState("IoT-сесій поки немає", "Відправ тестову сесію або запусти IoT-клієнт.")
            return
        }
        for (i in 0 until arr.length()) {
            val s = arr.getJSONObject(i)
            dataCard(
                title = "Сесія #${s.optInt("id")}",
                subtitle = "Авто: ${s.optString("carId", "-")}\nСередня швидкість: ${s.optDouble("avgSpeed", 0.0)} км/год\nПодії: гальмування ${s.optInt("hardBrakesTotal")}, смуга ${s.optInt("laneViolationsTotal")}, швидкість ${s.optInt("overspeedEventsTotal")}",
                badge = s.optString("category", "Unknown")
            )
        }
    }


    private fun renderCreatedUser(json: JSONObject) {
        root.removeViewSafe(output)
        resultHeader("Користувача створено", "Обліковий запис додано до системи HiveTrack")
        dataCard(
            title = json.optString("fullName", "Користувач"),
            subtitle = "Email: ${json.optString("email", "-")}\nРоль: ${roleUk(json.optString("role"))}",
            badge = "ID ${json.optInt("id")}"
        )
    }

    private fun renderCreatedGroup(json: JSONObject) {
        root.removeViewSafe(output)
        resultHeader("Групу створено", "Нова навчальна група збережена в базі даних")
        dataCard(
            title = json.optString("name", "Група"),
            subtitle = "Категорія: ${json.optString("category", "-")}\nПочаток: ${json.optString("startDate", "-")}\nЗавершення: ${json.optString("endDate", "-")}",
            badge = "ID ${json.optInt("id")}"
        ) { loadLessonsByGroup(json.optInt("id"), false) }
    }

    private fun renderCreatedLesson(json: JSONObject) {
        root.removeViewSafe(output)
        resultHeader("Заняття створено", "Розклад оновлено, запис доступний у відповідній групі")
        dataCard(
            title = json.optString("topic", "Заняття"),
            subtitle = "Тип: ${lessonTypeUk(json.optString("type"))}\nГрупа ID: ${json.optInt("groupId")}\nІнструктор ID: ${json.optInt("instructorId")}\nДата: ${formatDate(json.optString("lessonDateTime"))}",
            badge = "ID ${json.optInt("id")}"
        ) { loadAttendanceByLesson(json.optInt("id")) }
    }

    private fun renderCreatedAttendance(json: JSONObject) {
        root.removeViewSafe(output)
        resultHeader("Відвідуваність збережено", "Запис буде відображатися в історії учня")
        dataCard(
            title = "Учень ID ${json.optInt("studentId")}",
            subtitle = "Заняття ID: ${json.optInt("lessonId")}\nСтатус: ${statusUk(json.optString("status"))}\nКоментар: ${json.optString("comment", "-")}",
            badge = "ID ${json.optInt("id")}"
        ) { loadAttendanceByLesson(json.optInt("lessonId")) }
    }

    private fun renderIotCreated(json: JSONObject) {
        root.removeViewSafe(output)
        resultHeader("IoT-сесію відправлено", "Телеметрію успішно збережено на сервері")
        dataCard(
            title = "Сесія #${json.optInt("id")}",
            subtitle = json.optString("message", "Дані прийнято сервером"),
            badge = "OK"
        ) { loadIotSessions() }
    }

    private fun resultHeader(title: String, subtitle: String) {
        card(accentSoft, borderColor = Color.rgb(233, 203, 132), padding = 16) {
            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(ink)
            })
            addView(TextView(this@MainActivity).apply {
                text = subtitle
                textSize = 13f
                setTextColor(accentDark)
                setPadding(0, dp(6), 0, 0)
            })
        }
    }

    // UI helpers
    private data class Action(val title: String, val subtitle: String, val icon: String, val action: () -> Unit)

    private fun actionGrid(actions: List<Action>) {
        var row: LinearLayout? = null
        for ((index, action) in actions.withIndex()) {
            if (index % 2 == 0) {
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                root.addView(row, matchWrap(mBottom = 10))
            }
            row!!.addView(actionCard(action), LinearLayout.LayoutParams(0, dp(116), 1f).apply {
                setMargins(if (index % 2 == 0) 0 else dp(6), 0, if (index % 2 == 0) dp(6) else 0, 0)
            })
        }
    }

    private fun actionCard(a: Action): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = round(surface, 18f, line)
            setOnClickListener { a.action() }
            addView(TextView(this@MainActivity).apply { text = a.icon; textSize = 24f; setTextColor(accent) })
            addView(TextView(this@MainActivity).apply { text = a.title; textSize = 16f; typeface = Typeface.DEFAULT_BOLD; setTextColor(ink); setPadding(0, dp(7), 0, 0) })
            addView(TextView(this@MainActivity).apply { text = a.subtitle; textSize = 12f; setTextColor(muted); setPadding(0, dp(2), 0, 0) })
        }
    }

    private fun dataCard(title: String, subtitle: String, badge: String, onClick: (() -> Unit)? = null) {
        card(padding = 16) {
            val top = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            addView(top, matchWrap())
            top.addView(TextView(this@MainActivity).apply { text = title; textSize = 17f; typeface = Typeface.DEFAULT_BOLD; setTextColor(ink) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            top.addView(tag(badge, accentDark, accentSoft))
            addView(TextView(this@MainActivity).apply { text = subtitle; textSize = 13f; setTextColor(muted); setPadding(0, dp(8), 0, 0) })
            if (onClick != null) {
                addView(TextView(this@MainActivity).apply { text = "Натисни, щоб відкрити деталі"; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; setTextColor(accentDark); setPadding(0, dp(10), 0, 0) })
                setOnClickListener { onClick() }
            }
        }
    }

    private fun emptyState(title: String, message: String) {
        card(accentSoft, borderColor = Color.rgb(233, 203, 132), padding = 16) {
            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(ink)
            })
            addView(TextView(this@MainActivity).apply {
                text = message
                textSize = 13f
                setTextColor(accentDark)
                setPadding(0, dp(8), 0, 0)
            })
        }
    }

    private fun card(color: Int = surface, border: Int = 1, borderColor: Int = line, padding: Int = 18, content: LinearLayout.() -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
            background = if (border == 0) round(color, 20f) else round(color, 20f, borderColor)
            content()
        }
        root.addView(layout, matchWrap(mBottom = 14))
    }

    private fun input(label: String, value: String = "", type: Int = InputType.TYPE_CLASS_TEXT): EditText {
        TextView(this).apply {
            text = label
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
            setPadding(0, dp(8), 0, dp(6))
            root.addView(this)
        }
        return EditText(this).apply {
            hint = label
            setText(value)
            inputType = type
            setSingleLine(true)
            textSize = 15f
            setTextColor(ink)
            setHintTextColor(muted)
            setPadding(dp(14), 0, dp(14), 0)
            background = round(Color.rgb(254, 252, 248), 14f, line)
            root.addView(this, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).applyMargins(0, 0, 0, dp(8)))
        }
    }

    private fun inputInline(parent: LinearLayout, label: String, value: String = ""): EditText {
        return EditText(this).apply {
            hint = label
            setText(value)
            setSingleLine(true)
            textSize = 14f
            setTextColor(ink)
            setHintTextColor(muted)
            setPadding(dp(12), 0, dp(12), 0)
            background = round(Color.rgb(254, 252, 248), 12f, line)
            parent.addView(this, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)).applyMargins(0, dp(8), 0, 0))
        }
    }

    private fun button(text: String, primary: Boolean = false, action: () -> Unit) {
        root.addView(styledButton(text, primary, action), matchWrap(mTop = 6, mBottom = 8))
    }

    private fun styledButton(textValue: String, primary: Boolean, action: () -> Unit): Button {
        return Button(this).apply {
            text = textValue
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            setTextColor(if (primary) Color.WHITE else ink)
            background = if (primary) round(dark, 14f) else round(surface, 14f, line)
            setOnClickListener { action() }
        }
    }

    private fun sectionTitle(value: String) {
        root.addView(TextView(this).apply {
            text = value
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
            setPadding(dp(2), dp(8), dp(2), dp(10))
        })
    }

    private fun smallText(value: String) {
        root.addView(TextView(this).apply {
            text = value
            textSize = 12f
            setTextColor(muted)
            setPadding(dp(2), dp(4), dp(2), dp(14))
        })
    }

    private fun tag(value: String, textColor: Int, bgColor: Int): TextView {
        return TextView(this).apply {
            text = value
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColor)
            setPadding(dp(9), dp(4), dp(9), dp(4))
            background = round(bgColor, 7f)
        }
    }

    private fun iconButton(label: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(ink)
            background = round(surface, 14f, line)
            setOnClickListener { action() }
        }
    }

    private fun hexLogo(size: Int): TextView {
        return TextView(this).apply {
            text = "⬢"
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(accent)
            background = round(accentSoft, 14f, Color.rgb(229, 203, 143))
            layoutParams = ViewGroup.LayoutParams(size, size)
        }
    }

    private fun addOutput(textValue: String = "Результати запитів будуть показані тут.") {
        output = TextView(this).apply {
            text = textValue
            textSize = 13f
            typeface = Typeface.MONOSPACE
            setTextColor(muted)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = round(Color.rgb(254, 252, 248), 16f, line)
        }
        root.addView(output, matchWrap(mTop = 8, mBottom = 12))
    }

    private fun showSuccess(title: String, message: String) {
        if (!::output.isInitialized) addOutput()
        output.setTextColor(good)
        output.text = "$title\n\n$message"
    }

    private fun showError(title: String, message: String) {
        if (!::output.isInitialized) addOutput()
        output.setTextColor(danger)
        output.text = "$title\n\n$message"
    }

    private fun setLoading(value: String) {
        if (!::output.isInitialized) addOutput()
        output.setTextColor(muted)
        output.text = value
    }

    private fun runApi(block: () -> Unit) {
        Thread {
            try {
                block()
            } catch (e: ApiException) {
                ui { showError("Помилка API ${e.statusCode}", friendlyError(e.message ?: "Невідома помилка сервера")) }
            } catch (e: Exception) {
                ui { showError("Помилка", e.message ?: "Невідома помилка") }
            }
        }.start()
    }

    private fun formatObject(json: JSONObject): String {
        val lines = mutableListOf<String>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            lines.add("${labelFor(key)}: ${json.opt(key)}")
        }
        return lines.joinToString("\n")
    }

    private fun labelFor(key: String): String = when (key) {
        "id" -> "ID"
        "fullName" -> "ПІБ"
        "email" -> "Email"
        "role" -> "Роль"
        "name" -> "Назва"
        "category" -> "Категорія"
        "startDate" -> "Дата початку"
        "endDate" -> "Дата завершення"
        "message" -> "Повідомлення"
        else -> key
    }

    private fun statusUk(value: String): String = when (value) {
        "Present" -> "Присутній"
        "Absent" -> "Відсутній"
        "Late" -> "Запізнився"
        else -> value.ifBlank { "-" }
    }

    private fun lessonTypeUk(value: String): String = when (value) {
        "Theory" -> "Теорія"
        "Practice" -> "Практика"
        else -> value.ifBlank { "-" }
    }

    private fun testModeUk(value: String): String = when (value) {
        "Training" -> "Тренування"
        "Exam" -> "Іспит"
        else -> value.ifBlank { "Тест" }
    }

    private fun testStatusUk(value: String): String = when (value) {
        "Completed" -> "завершено"
        "In progress" -> "у процесі"
        "Not started" -> "не розпочато"
        else -> value.ifBlank { "не розпочато" }
    }

    private fun actionUk(value: String): String = when (value) {
        "user_registered" -> "Реєстрація користувача"
        "group_created" -> "Створення групи"
        "lesson_created" -> "Створення заняття"
        "attendance_saved" -> "Збереження відвідуваності"
        "notification_created" -> "Створення сповіщення"
        "enrollment_created" -> "Зарахування учня"
        "enrollment_updated" -> "Оновлення зарахування"
        "enrollment_deleted" -> "Видалення зарахування"
        "test_attempt_created" -> "Проходження тесту"
        "iot_session_received" -> "Отримання IoT-сесії"
        "user_blocked" -> "Блокування користувача"
        "system_patch_applied" -> "Оновлення структури БД"
        else -> value.ifBlank { "Подія" }
    }

    private fun roleUk(value: String): String = when (value) {
        "Student" -> "Учень"
        "Instructor" -> "Інструктор"
        "Admin" -> "Адміністратор"
        else -> value.ifBlank { "Користувач" }
    }

    private fun friendlyError(value: String): String {
        return when {
            value.contains("Invalid email or password", true) -> "Невірний email або пароль. Перевір дані входу."
            value.contains("Group not found", true) -> "Групу не знайдено. Перевір ID групи або створи групу в кабінеті адміністратора."
            value.contains("Instructor not found", true) -> "Інструктора не знайдено. Перевір ID інструктора."
            value.contains("Student not found", true) -> "Учня не знайдено. Перевір ID учня."
            value.contains("Lesson not found", true) -> "Заняття не знайдено. Перевір ID заняття."
            value.contains("Selected user is not a student", true) -> "Обраний користувач не має ролі учня."
            value.contains("Selected user is not an instructor", true) -> "Обраний користувач не має ролі інструктора."
            value.contains("Unauthorized", true) || value.contains("token", true) -> "Сесія недійсна або завершилася. Вийди з акаунта та увійди знову."
            else -> value
        }
    }

    private fun formatDate(value: String): String = value.ifBlank { "-" }.replace("T", " ").replace(".000Z", "")
    private fun ui(block: () -> Unit) = runOnUiThread(block)
    private fun toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_SHORT).show()
    private fun firstName(): String = session.fullName.split(" ").firstOrNull()?.ifBlank { null } ?: "користувачу"
    private fun initials(value: String): String = value.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercaseChar().toString() }.ifBlank { "HT" }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun matchWrap(mTop: Int = 0, mBottom: Int = 0): LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, dp(mTop), 0, dp(mBottom)) }
    private fun addSpace(parent: LinearLayout, size: Int, horizontal: Boolean = false) { parent.addView(Space(this), if (horizontal) ViewGroup.LayoutParams(dp(size), 1) else ViewGroup.LayoutParams(1, dp(size))) }

    private fun round(color: Int, radius: Float, strokeColor: Int? = null): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius.toInt()).toFloat()
            if (strokeColor != null) setStroke(dp(1), strokeColor)
        }
    }
}

private fun ViewGroup.LayoutParams.applyMargins(l: Int, t: Int, r: Int, b: Int): ViewGroup.LayoutParams {
    if (this is ViewGroup.MarginLayoutParams) setMargins(l, t, r, b)
    return this
}

private fun ViewGroup.removeViewSafe(view: View?) {
    if (view != null && view.parent == this) removeView(view)
}
