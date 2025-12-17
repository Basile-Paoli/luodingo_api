package fr.ludodingo.repository

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable data class QuestionOption(val id: String, val text: String)

class CourseRepository(val db: Database) {

    object CoursesTable : IntIdTable() {
        val courseId = varchar("course_id", 50).uniqueIndex() // String ID like "en_beginner"
        val title = varchar("title", 100)
        val description = varchar("description", 255)
        val languageCode = varchar("language_code", 10)
        val level = varchar("level", 20) // BEGINNER, INTERMEDIATE, ADVANCED
        val colorHex = varchar("color_hex", 10)
    }

    object LessonsTable : IntIdTable() {
        val lessonId = varchar("lesson_id", 50).uniqueIndex()
        val course = reference("course", CoursesTable)
        val title = varchar("title", 100)
        val description = varchar("description", 255)
        val order = integer("order_index")
    }

    object QuestionsTable : IntIdTable() {
        val lesson = reference("lesson", LessonsTable)
        val type = varchar("type", 20) // MULTIPLE_CHOICE, FILL_GAP, etc.
        val statement = text("statement")
        val instruction = varchar("instruction", 255)
        val options = text("options") // JSON string
        val correctAnswer = varchar("correct_answer", 255)
        val order = integer("order_index")
    }

    init {
        transaction(db) {
            initTable(CoursesTable)
            initTable(LessonsTable)
            initTable(QuestionsTable)
            seedData()
        }
    }

    private fun seedData() {
        if (CoursesTable.selectAll().empty().not()) return

        // --- ENGLISH BEGINNER ---
        val c1 =
                CoursesTable.insertAndGetId {
                    it[courseId] = "c1"
                    it[title] = "Anglais - Découverte"
                    it[description] = "Découvrez les bases de l'anglais"
                    it[languageCode] = "en"
                    it[level] = "BEGINNER"
                    it[colorHex] = "0xFF58CC02"
                }

        // Lesson 1: Basics
        val l1 =
                LessonsTable.insertAndGetId {
                    it[lessonId] = "lesson_basics_1"
                    it[course] = c1
                    it[title] = "Bases : Salutations"
                    it[description] = "Apprenez à dire bonjour"
                    it[order] = 1
                }
        QuestionsTable.insert {
            it[lesson] = l1
            it[type] = "TRANSLATE"
            it[instruction] = "Traduisez cette phrase"
            it[statement] = "Hello, I am Duo."
            it[options] =
                    Json.encodeToString(
                            listOf("Bonjour", ",", "je", "suis", "Duo", "chat", "mange", "rouge")
                    )
            it[correctAnswer] = "Bonjour , je suis Duo"
            it[order] = 1
        }
        QuestionsTable.insert {
            it[lesson] = l1
            it[type] = "MULTIPLE_CHOICE"
            it[instruction] = "Comment dit-on 'Garçon' ?"
            it[statement] = ""
            it[options] = Json.encodeToString(listOf("Boy", "Girl", "Apple", "Car"))
            it[correctAnswer] = "Boy"
            it[order] = 2
        }
        QuestionsTable.insert {
            it[lesson] = l1
            it[type] = "FILL_GAP"
            it[instruction] = "Complétez la phrase"
            it[statement] = "I ____ a student."
            it[options] = "[]"
            it[correctAnswer] = "am"
            it[order] = 3
        }
        QuestionsTable.insert {
            it[lesson] = l1
            it[type] = "LISTENING"
            it[instruction] = "Écrivez ce que vous entendez"
            it[statement] = "" // audioUrl not supported in DB yet, simulating
            it[options] = "[]"
            it[correctAnswer] = "Hello"
            it[order] = 4
        }
        QuestionsTable.insert {
            it[lesson] = l1
            it[type] = "TRUE_FALSE"
            it[instruction] = "Vrai ou Faux ?"
            it[statement] = "Good night = Bonjour"
            it[options] = Json.encodeToString(listOf("Vrai", "Faux"))
            it[correctAnswer] = "Faux"
            it[order] = 5
        }

        // Lesson 2: Food
        val l2 =
                LessonsTable.insertAndGetId {
                    it[lessonId] = "lesson_food_1"
                    it[course] = c1
                    it[title] = "Nourriture"
                    it[description] = "Miam miam"
                    it[order] = 2
                }
        QuestionsTable.insert {
            it[lesson] = l2
            it[type] = "TRANSLATE"
            it[instruction] = "Traduisez"
            it[statement] = "I eat an apple."
            it[options] =
                    Json.encodeToString(listOf("Je", "mange", "une", "pomme", "bois", "eau", "la"))
            it[correctAnswer] = "Je mange une pomme"
            it[order] = 1
        }
        QuestionsTable.insert {
            it[lesson] = l2
            it[type] = "MULTIPLE_CHOICE"
            it[instruction] = "Que buvez-vous le matin ?"
            it[statement] = "Coffee"
            it[options] = Json.encodeToString(listOf("Coffee", "Bread", "Cheese", "Meat"))
            it[correctAnswer] = "Coffee"
            it[order] = 2
        }
        QuestionsTable.insert {
            it[lesson] = l2
            it[type] = "FILL_GAP"
            it[instruction] = "Complétez"
            it[statement] = "She ____ water."
            it[options] = "[]"
            it[correctAnswer] = "drinks"
            it[order] = 3
        }
        QuestionsTable.insert {
            it[lesson] = l2
            it[type] = "TRANSLATE"
            it[instruction] = "Traduisez"
            it[statement] = "The bread is good."
            it[options] =
                    Json.encodeToString(
                            listOf("Le", "pain", "est", "bon", "mauvais", "lait", "sel")
                    )
            it[correctAnswer] = "Le pain est bon"
            it[order] = 4
        }

        // Lesson 3: Animals
        val l3 =
                LessonsTable.insertAndGetId {
                    it[lessonId] = "lesson_animals_1"
                    it[course] = c1
                    it[title] = "Animaux"
                    it[description] = "Nos amis les bêtes"
                    it[order] = 3
                }
        QuestionsTable.insert {
            it[lesson] = l3
            it[type] = "MULTIPLE_CHOICE"
            it[instruction] = "Quel animal aboie ?"
            it[statement] = "Dog"
            it[options] = Json.encodeToString(listOf("Cat", "Dog", "Bird", "Fish"))
            it[correctAnswer] = "Dog"
            it[order] = 1
        }
        QuestionsTable.insert {
            it[lesson] = l3
            it[type] = "TRANSLATE"
            it[instruction] = "Traduisez"
            it[statement] = "The cat is black."
            it[options] =
                    Json.encodeToString(
                            listOf("Le", "chat", "est", "noir", "blanc", "chien", "souris")
                    )
            it[correctAnswer] = "Le chat est noir"
            it[order] = 2
        }
        QuestionsTable.insert {
            it[lesson] = l3
            it[type] = "TRUE_FALSE"
            it[instruction] = "Vrai ou Faux ?"
            it[statement] = "Bird = Oiseau"
            it[options] = Json.encodeToString(listOf("Vrai", "Faux"))
            it[correctAnswer] = "Vrai"
            it[order] = 3
        }

        // --- ENGLISH INTERMEDIATE ---
        val c2 =
                CoursesTable.insertAndGetId {
                    it[courseId] = "c2"
                    it[title] = "Anglais - Voyage"
                    it[description] = "Gérer ses déplacements"
                    it[languageCode] = "en"
                    it[level] = "INTERMEDIATE"
                    it[colorHex] = "0xFFCE82FF"
                }

        // Lesson 1: Airport
        val l4 =
                LessonsTable.insertAndGetId {
                    it[lessonId] = "lesson_travel_1"
                    it[course] = c2
                    it[title] = "Aéroport"
                    it[description] = "Prêt au décollage"
                    it[order] = 1
                }
        QuestionsTable.insert {
            it[lesson] = l4
            it[type] = "TRANSLATE"
            it[instruction] = "Traduisez"
            it[statement] = "Where is the passport?"
            it[options] =
                    Json.encodeToString(
                            listOf("Où", "est", "le", "passeport", "mon", "ton", "valise", "?")
                    )
            it[correctAnswer] = "Où est le passeport ?"
            it[order] = 1
        }
        QuestionsTable.insert {
            it[lesson] = l4
            it[type] = "MULTIPLE_CHOICE"
            it[instruction] = "Choisissez la bonne réponse"
            it[statement] = "Here is your boarding pass."
            it[options] =
                    Json.encodeToString(listOf("Thank you", "I am sorry", "Good night", "Apple"))
            it[correctAnswer] = "Thank you"
            it[order] = 2
        }
        QuestionsTable.insert {
            it[lesson] = l4
            it[type] = "FILL_GAP"
            it[instruction] = "Complétez"
            it[statement] = "The flight is ____ time."
            it[options] = "[]"
            it[correctAnswer] = "on"
            it[order] = 3
        }
        QuestionsTable.insert {
            it[lesson] = l4
            it[type] = "TRANSLATE"
            it[instruction] = "Traduisez"
            it[statement] = "I need a taxi."
            it[options] =
                    Json.encodeToString(
                            listOf("J'ai", "besoin", "d'un", "taxi", "bus", "train", "vlo")
                    )
            it[correctAnswer] = "J'ai besoin d'un taxi"
            it[order] = 4
        }

        // Lesson 2: Directions
        val l5 =
                LessonsTable.insertAndGetId {
                    it[lessonId] = "lesson_travel_2"
                    it[course] = c2
                    it[title] = "Directions"
                    it[description] = "Ne vous perdez pas"
                    it[order] = 2
                }
        QuestionsTable.insert {
            it[lesson] = l5
            it[type] = "MULTIPLE_CHOICE"
            it[instruction] = "Où est la banque ?"
            it[statement] = "Where is the bank?"
            it[options] =
                    Json.encodeToString(
                            listOf("It is on the left", "It is a fruit", "My name is Duo", "Yes")
                    )
            it[correctAnswer] = "It is on the left"
            it[order] = 1
        }
        QuestionsTable.insert {
            it[lesson] = l5
            it[type] = "FILL_GAP"
            it[instruction] = "Complétez"
            it[statement] = "Turn ____ at the corner."
            it[options] = "[]"
            it[correctAnswer] = "right"
            it[order] = 2
        }

        // --- ENGLISH ADVANCED ---
        val c3 =
                CoursesTable.insertAndGetId {
                    it[courseId] = "c3"
                    it[title] = "Anglais - Pro"
                    it[description] = "Le monde du travail"
                    it[languageCode] = "en"
                    it[level] = "ADVANCED"
                    it[colorHex] = "0xFFFF9600"
                }

        // Lesson 1: Business
        val l6 =
                LessonsTable.insertAndGetId {
                    it[lessonId] = "lesson_pro_1"
                    it[course] = c3
                    it[title] = "Réunion"
                    it[description] = "Business is business"
                    it[order] = 1
                }
        QuestionsTable.insert {
            it[lesson] = l6
            it[type] = "TRANSLATE"
            it[instruction] = "Traduisez"
            it[statement] = "We need to schedule a meeting."
            it[options] =
                    Json.encodeToString(
                            listOf(
                                    "Nous",
                                    "devons",
                                    "planifier",
                                    "une",
                                    "réunion",
                                    "manger",
                                    "dormir",
                                    "chat"
                            )
                    )
            it[correctAnswer] = "Nous devons planifier une réunion"
            it[order] = 1
        }
        QuestionsTable.insert {
            it[lesson] = l6
            it[type] = "TRUE_FALSE"
            it[instruction] = "Is this professional?"
            it[statement] = "Yo, wassup boss?"
            it[options] = Json.encodeToString(listOf("Yes", "No"))
            it[correctAnswer] = "No"
            it[order] = 2
        }
        QuestionsTable.insert {
            it[lesson] = l6
            it[type] = "MULTIPLE_CHOICE"
            it[instruction] = "Choose the best closing"
            it[statement] = "Email closing"
            it[options] = Json.encodeToString(listOf("Best regards", "Love you", "See ya", "Bye"))
            it[correctAnswer] = "Best regards"
            it[order] = 3
        }

        // --- PLACEMENT TEST ---
        // We create a hidden course for it or just a lesson without course?
        // DB schema requires a course. Let's create a "Placement" course.
        val cPlacement =
                CoursesTable.insertAndGetId {
                    it[courseId] = "placement"
                    it[title] = "Placement"
                    it[description] = "Test de niveau"
                    it[languageCode] = "en"
                    it[level] = "UNKNOWN"
                    it[colorHex] = "0xFFFFFFFF"
                }

        val lPt =
                LessonsTable.insertAndGetId {
                    it[lessonId] = "placement_test"
                    it[course] = cPlacement
                    it[title] = "Test de niveau"
                    it[description] = "Évaluez votre niveau"
                    it[order] = 1
                }
        QuestionsTable.insert {
            it[lesson] = lPt
            it[type] = "TRANSLATE"
            it[instruction] = "Traduisez"
            it[statement] = "Hello"
            it[options] = Json.encodeToString(listOf("Bonjour", "Au revoir", "Chat"))
            it[correctAnswer] = "Bonjour"
            it[order] = 1
        }
        QuestionsTable.insert {
            it[lesson] = lPt
            it[type] = "MULTIPLE_CHOICE"
            it[instruction] = "Choose the correct word"
            it[statement] = "I ____ a student"
            it[options] = Json.encodeToString(listOf("am", "is", "are"))
            it[correctAnswer] = "am"
            it[order] = 2
        }
        QuestionsTable.insert {
            it[lesson] = lPt
            it[type] = "TRANSLATE"
            it[instruction] = "Traduisez"
            it[statement] = "I need a taxi"
            it[options] =
                    Json.encodeToString(
                            listOf(
                                    "J'ai",
                                    "besoin",
                                    "d'un",
                                    "taxi",
                                    "Je",
                                    "veux",
                                    "un",
                                    "taxi",
                                    "Taxi"
                            )
                    )
            it[correctAnswer] = "J'ai besoin d'un taxi"
            it[order] = 3
        }
    }

    suspend fun getCourses(languageCode: String? = null): List<CourseDto> =
            newSuspendedTransaction(db = db) {
                val query =
                        if (languageCode != null) {
                            CoursesTable.selectAll().where {
                                CoursesTable.languageCode eq languageCode
                            }
                        } else {
                            CoursesTable.selectAll()
                        }

                query.map { row ->
                    val cId = row[CoursesTable.id]
                    val lessons =
                            LessonsTable.selectAll()
                                    .where { LessonsTable.course eq cId }
                                    .orderBy(LessonsTable.order to SortOrder.ASC)
                                    .map { lRow ->
                                        LessonDto(
                                                id = lRow[LessonsTable.lessonId],
                                                title = lRow[LessonsTable.title],
                                                description = lRow[LessonsTable.description],
                                                order = lRow[LessonsTable.order]
                                        )
                                    }

                    CourseDto(
                            id = row[CoursesTable.courseId],
                            title = row[CoursesTable.title],
                            description = row[CoursesTable.description],
                            level = row[CoursesTable.level],
                            color = row[CoursesTable.colorHex],
                            lessons = lessons
                    )
                }
            }

    suspend fun getLesson(lessonId: String): LessonDetailDto? =
            newSuspendedTransaction(db = db) {
                val lessonRow =
                        LessonsTable.selectAll()
                                .where { LessonsTable.lessonId eq lessonId }
                                .firstOrNull()
                                ?: return@newSuspendedTransaction null
                val lId = lessonRow[LessonsTable.id]

                val questions =
                        QuestionsTable.selectAll()
                                .where { QuestionsTable.lesson eq lId }
                                .orderBy(QuestionsTable.order to SortOrder.ASC)
                                .map { qRow ->
                                    QuestionDto(
                                            id = qRow[QuestionsTable.id].value.toString(),
                                            type = qRow[QuestionsTable.type],
                                            statement = qRow[QuestionsTable.statement],
                                            instruction = qRow[QuestionsTable.instruction],
                                            options =
                                                    try {
                                                        Json.decodeFromString<List<String>>(
                                                                        qRow[QuestionsTable.options]
                                                                )
                                                                .shuffled()
                                                    } catch (e: Exception) {
                                                        emptyList()
                                                    },
                                            correctAnswer = qRow[QuestionsTable.correctAnswer]
                                    )
                                }

                LessonDetailDto(
                        id = lessonRow[LessonsTable.lessonId],
                        title = lessonRow[LessonsTable.title],
                        description = lessonRow[LessonsTable.description],
                        questions = questions
                )
            }
}

@Serializable
data class CourseDto(
        val id: String,
        val title: String,
        val description: String,
        val level: String,
        val color: String,
        val lessons: List<LessonDto>
)

@Serializable
data class LessonDto(val id: String, val title: String, val description: String, val order: Int)

@Serializable
data class LessonDetailDto(
        val id: String,
        val title: String,
        val description: String,
        val questions: List<QuestionDto>
)

@Serializable
data class QuestionDto(
        val id: String,
        val type: String,
        val statement: String,
        val instruction: String,
        val options: List<String>,
        val correctAnswer: String
)
