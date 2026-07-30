package com.tetsushozawa.dailylivingcompass

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetsushozawa.dailylivingcompass.ui.theme.DailyLivingCompassTheme
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyLivingCompassTheme {
                DailyLivingCompassApp()
            }
        }
    }
}

private enum class AppStep {
    Top,
    SavedRecords,
    RecoveryProgress,
    RecoveryMandala,
    SocialActivityCompassInfo,
    BasicInfo,
    SelfCheck,
    AbilityRange,
    Result,
    BeforeStart,
    DeepBreathingLevel1,
    DeepBreathingLevel2,
    StandingLevel3,
    DeepSquatLevel32,
    IndoorWalkingLevel4,
    Program2IndoorWalking,
    FamilyProgramCompleted
}

internal data class SelfCheckState(
    val pain: Int = 0,
    val fatigue: Int = 0,
    val sleep: Int = 0,
    val breathing: Int = 0,
    val comparison: Int = 0,
    val getUp: Int = 0,
    val sit: Int = 0,
    val stand: Int = 0,
    val indoorWalk: Int = 0,
    val outdoor: Int = 0
)

private data class ProgramLevel(
    val name: String,
    val message: String
)

private data class ExerciseProgram(
    val programName: String,
    val level: String,
    val content: String,
    val countChoices: List<String>
)

private data class SavedExerciseRecord(
    val storageIndex: Int,
    val date: String,
    val time: String,
    val level: String,
    val programName: String,
    val content: String,
    val count: String,
    val selfEvaluation: String,
    val preExerciseMemo: String,
    val dizziness: String,
    val breathlessness: String,
    val strongPain: String,
    val fallRisk: String,
    val startPain: String,
    val startFatigue: String,
    val startSleep: String,
    val startBreathing: String,
    val startComparison: String,
    val startGetUp: String,
    val startSit: String,
    val startStand: String,
    val startIndoorWalk: String,
    val startOutdoorWalk: String,
    val nextDayWorse: String,
    val nextCriteria: String,
    val backCriteria: String
) {
    val hasStartCondition: Boolean
        get() = listOf(
            startPain,
            startFatigue,
            startSleep,
            startBreathing,
            startComparison,
            startGetUp,
            startSit,
            startStand,
            startIndoorWalk,
            startOutdoorWalk
        ).any { it.isNotBlank() }

    val programCategory: String
        get() = if (programName == BasicRecoveryProgramName || programName == WalkingProgramName) {
            programName
        } else if (level.startsWith("社会復帰編") || programName == "社会復帰プログラム") {
            "社会復帰プログラム"
        } else {
            "家庭生活復帰プログラム"
        }

    val exerciseName: String
        get() = exerciseNameForRecord(programName, level)
}

internal data class MandalaTrajectoryPoint(
    val cell: Pair<Int, Int>,
    val isLatest: Boolean
)

internal data class MandalaStartState(
    val fatigue: String,
    val getUp: String,
    val sit: String,
    val stand: String,
    val indoorWalk: String,
    val outdoorWalk: String
)

@Composable
private fun DailyLivingCompassApp() {
    var step by remember { mutableStateOf(AppStep.Top) }
    var checkState by remember { mutableStateOf(SelfCheckState()) }
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var basicMemo by remember { mutableStateOf("") }
    var level by remember { mutableStateOf<ProgramLevel?>(null) }
    var selectedProgramName by remember { mutableStateOf(BasicRecoveryProgramName) }
    var scrollResetKey by remember { mutableStateOf(0) }
    val scrollState = remember(step, scrollResetKey) { ScrollState(0) }

    LaunchedEffect(step, scrollResetKey) {
        scrollState.scrollTo(0)
    }

    Scaffold { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ScreenBackground),
            color = ScreenBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (step) {
                    AppStep.Top -> TopScreen(
                        onStart = {
                            val now = Date()
                            dateText = SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now)
                            timeText = SimpleDateFormat("HH:mm", Locale.JAPAN).format(now)
                            basicMemo = ""
                            step = AppStep.BasicInfo
                        },
                        onShowRecords = { step = AppStep.SavedRecords },
                        onShowRecoveryProgress = { step = AppStep.RecoveryProgress },
                        onShowRecoveryMandala = { step = AppStep.RecoveryMandala }
                    )

                    AppStep.SavedRecords -> SavedRecordsScreen(
                        onBack = { step = AppStep.Top },
                        onResetScroll = { scrollResetKey++ }
                    )

                    AppStep.RecoveryProgress -> RecoveryProgressScreen(
                        onBack = { step = AppStep.Top }
                    )

                    AppStep.RecoveryMandala -> RecoveryMandalaScreen(
                        onBack = { step = AppStep.Top },
                        onShowSocialActivityCompass = { step = AppStep.SocialActivityCompassInfo }
                    )

                    AppStep.SocialActivityCompassInfo -> SocialActivityCompassInfoScreen(
                        onBack = { step = AppStep.RecoveryMandala }
                    )

                    AppStep.BasicInfo -> BasicInfoScreen(
                        dateText = dateText,
                        timeText = timeText,
                        memo = basicMemo,
                        onMemoChange = { basicMemo = it },
                        onNext = { step = AppStep.SelfCheck }
                    )

                    AppStep.SelfCheck -> SelfCheckScreen(
                        state = checkState,
                        onStateChange = { checkState = it },
                        onNext = { step = AppStep.AbilityRange }
                    )

                    AppStep.AbilityRange -> AbilityRangeScreen(
                        state = checkState,
                        onStateChange = { checkState = it },
                        onBack = { step = AppStep.SelfCheck },
                        onEvaluate = {
                            level = judgeProgramLevel(checkState)
                            step = AppStep.Result
                        }
                    )

                    AppStep.Result -> ResultScreen(
                        level = level ?: judgeProgramLevel(checkState),
                        onNext = { step = AppStep.BeforeStart },
                        onRecheck = { step = AppStep.SelfCheck }
                    )

                    AppStep.BeforeStart -> BeforeStartScreen(
                        onStartProgram1 = {
                            selectedProgramName = BasicRecoveryProgramName
                            step = AppStep.DeepBreathingLevel1
                        },
                        onStartProgram2 = {
                            selectedProgramName = WalkingProgramName
                            step = AppStep.Program2IndoorWalking
                        }
                    )

                    AppStep.DeepBreathingLevel1 -> DeepBreathingLevel1Screen(
                        dateText = dateText,
                        timeText = timeText,
                        preExerciseMemo = basicMemo,
                        startState = checkState,
                        recordProgramName = selectedProgramName,
                        onBackToTop = { step = AppStep.Top },
                        onNext = { step = AppStep.DeepBreathingLevel2 }
                    )

                    AppStep.DeepBreathingLevel2 -> DeepBreathingLevel2Screen(
                        dateText = dateText,
                        timeText = timeText,
                        preExerciseMemo = basicMemo,
                        startState = checkState,
                        recordProgramName = selectedProgramName,
                        onBackToTop = { step = AppStep.Top },
                        onNext = { step = AppStep.StandingLevel3 }
                    )

                    AppStep.StandingLevel3 -> StandingLevel3Screen(
                        dateText = dateText,
                        timeText = timeText,
                        preExerciseMemo = basicMemo,
                        startState = checkState,
                        recordProgramName = selectedProgramName,
                        onBackToTop = { step = AppStep.Top },
                        onNext = { step = AppStep.DeepSquatLevel32 }
                    )

                    AppStep.DeepSquatLevel32 -> DeepSquatLevel32Screen(
                        dateText = dateText,
                        timeText = timeText,
                        preExerciseMemo = basicMemo,
                        startState = checkState,
                        recordProgramName = selectedProgramName,
                        onBackToTop = { step = AppStep.Top },
                        onNext = { step = AppStep.IndoorWalkingLevel4 }
                    )

                    AppStep.IndoorWalkingLevel4 -> IndoorWalkingLevel4Screen(
                        dateText = dateText,
                        timeText = timeText,
                        preExerciseMemo = basicMemo,
                        startState = checkState,
                        recordProgramName = selectedProgramName,
                        onBackToTop = { step = AppStep.Top },
                        onComplete = { step = AppStep.FamilyProgramCompleted }
                    )

                    AppStep.Program2IndoorWalking -> IndoorWalkingLevel4Screen(
                        dateText = dateText,
                        timeText = timeText,
                        preExerciseMemo = basicMemo,
                        startState = checkState,
                        recordProgramName = selectedProgramName,
                        onBackToTop = { step = AppStep.Top },
                        onComplete = { step = AppStep.FamilyProgramCompleted },
                        onNavigateBack = { step = AppStep.BeforeStart }
                    )

                    AppStep.FamilyProgramCompleted -> FamilyProgramCompletedScreen(
                        onBackToTop = { step = AppStep.Top }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopScreen(
    onStart: () -> Unit,
    onShowRecords: () -> Unit,
    onShowRecoveryProgress: () -> Unit,
    onShowRecoveryMandala: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.life_recovery_compass_icon),
            contentDescription = "生活回復コンパス",
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )
    }
    Text(
        text = "生活回復コンパス",
        modifier = Modifier.fillMaxWidth(),
        color = TextPrimary,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 34.sp,
        textAlign = TextAlign.Center
    )
    Text(
        text = "日常生活への回復を、一歩ずつ見える形にするプログラム",
        color = TextPrimary,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
    MessageCard(
        text = "このアプリは現在の能力を評価したり、患者同士を比較するためのものではありません。\n\n現在の状態は、評価ではなく出発点です。\n\n一人の患者も見捨てないことを基本理念とします。"
    )
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PastelGreenButton,
                contentColor = Color.White
            )
        ) {
            Text("家庭生活復帰プログラム")
        }
        OutlinedButton(
            onClick = onShowRecords,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
        ) {
            Text("記録を見る")
        }
        OutlinedButton(
            onClick = onShowRecoveryProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
        ) {
            Text("回復経過を見る")
        }
        Button(
            onClick = onShowRecoveryMandala,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MandalaPurpleButton,
                contentColor = Color.White
            )
        ) {
            Text("回復曼荼羅を見る")
        }
    }
}

@Composable
private fun SavedRecordsScreen(
    onBack: () -> Unit,
    onResetScroll: () -> Unit
) {
    val context = LocalContext.current
    var allRecords by remember { mutableStateOf(loadExerciseRecordsSafely(context)) }
    val records = allRecords
    var selectedRecord by remember { mutableStateOf<SavedExerciseRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<SavedExerciseRecord?>(null) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }

    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "この記録を削除しますか？",
                    color = Color.Black
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = recordToDelete
                        if (target != null && deleteExerciseRecordSafely(context, target.storageIndex)) {
                            allRecords = loadExerciseRecordsSafely(context)
                            onResetScroll()
                        }
                        recordToDelete = null
                    }
                ) {
                    Text("削除", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("キャンセル", color = Color.Black)
                }
            }
        )
    }

    if (showDeleteAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirmation = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "すべての記録を削除しますか？",
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "保存されているすべての記録を削除します。\n削除した記録は元に戻せません。",
                    color = Color.Black
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (deleteAllExerciseRecordsSafely(context)) {
                            allRecords = emptyList()
                            selectedRecord = null
                            recordToDelete = null
                            onResetScroll()
                        }
                        showDeleteAllConfirmation = false
                    }
                ) {
                    Text(
                        text = "すべて削除",
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirmation = false }) {
                    Text("キャンセル", color = Color.Black)
                }
            }
        )
    }

    val record = selectedRecord
    if (record != null) {
        SavedRecordDetailScreen(
            record = record,
            onBack = {
                selectedRecord = null
                onResetScroll()
            }
        )
    } else {
        ScreenTitle("記録を見る")
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("戻る")
        }
        if (records.isEmpty()) {
            MessageCard(text = "記録はありません")
        } else {
            records.forEach { savedRecord ->
                SavedRecordListItem(
                    record = savedRecord,
                    onClick = {
                        selectedRecord = savedRecord
                        onResetScroll()
                    },
                    onDelete = { recordToDelete = savedRecord }
                )
            }
        }
        if (shouldShowDeleteAllRecords(records.size)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { showDeleteAllConfirmation = true },
                    modifier = Modifier.heightIn(min = 40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "すべての記録を削除",
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("戻る")
        }
    }
}

@Composable
private fun RecoveryProgressScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val records = remember { loadExerciseRecordsSafely(context) }

    ScreenTitle("回復経過を見る")
    OutlinedButton(
        onClick = onBack,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text("戻る")
    }
    if (records.isEmpty()) {
        MessageCard(text = "まだ保存された記録はありません。")
    } else {
        records.forEach { record ->
            RecoveryProgressListItem(record = record)
        }
    }
    OutlinedButton(
        onClick = onBack,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text("戻る")
    }
}

@Composable
private fun RecoveryProgressListItem(record: SavedExerciseRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = recordListDate(record.date),
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
            Text(
                text = record.programCategory,
                color = TextPrimary,
                lineHeight = 22.sp
            )
            Text(
                text = "${recordListLevel(record.level)}　${recordEvaluationMark(record.selfEvaluation)}",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun RecoveryMandalaScreen(
    onBack: () -> Unit,
    onShowSocialActivityCompass: () -> Unit
) {
    val context = LocalContext.current
    val records = remember { loadExerciseRecordsSafely(context) }
    val trajectory = remember(records) {
        recoveryMandalaTrajectory(records)
    }
    val showSocialActivityGuidance = remember(records) {
        shouldShowSocialActivityGuidanceForRecords(records)
    }

    ScreenTitle("回復曼荼羅")
    MessageCard(text = "開始時の動ける範囲と疲労から、生活回復の軌跡を表示します。外枠付きの点が最新の記録です。")
    RecoveryMandalaGrid(trajectory = trajectory)
    if (showSocialActivityGuidance) {
        SocialActivityGuidanceCard(onShowSocialActivityCompass)
    }
    OutlinedButton(
        onClick = onBack,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text("戻る")
    }
}

@Composable
private fun SocialActivityGuidanceCard(onShowSocialActivityCompass: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "次の段階について",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
            Text(
                text = "屋外での生活が安定してきています。\n通勤・外出・仕事などの社会活動を記録する場合は、\n社会活動コンパスへ進むことができます。",
                color = TextPrimary,
                lineHeight = 24.sp
            )
            PrimaryButton(
                text = "社会活動コンパスについて見る",
                onClick = onShowSocialActivityCompass
            )
        }
    }
}

@Composable
private fun SocialActivityCompassInfoScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    ScreenTitle("社会活動コンパス")
    MessageCard(
        text = "社会活動コンパスは、\n屋外活動が安定した後に、\n通勤、外出、職場での活動などを記録するためのアプリです。\n\n生活回復コンパスの記録はそのまま残ります。\n無理に移行する必要はありません。"
    )
    OutlinedButton(
        onClick = onBack,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text("戻る")
    }
}

@Composable
private fun RecoveryMandalaGrid(
    trajectory: List<MandalaTrajectoryPoint>
) {
    val columns = listOf("起き上がる", "座る", "立つ", "屋内", "屋外")
    val rows = listOf(1, 2, 3, 4, 5)
    Text(
        text = "動ける範囲",
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        lineHeight = 22.sp
    )
    Text(
        text = "疲労（上：少ない／下：強い）",
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        lineHeight = 22.sp
    )
    val axisWeight = 0.5f
    val cellWeight = 1f
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MandalaCell(text = "", modifier = Modifier.weight(axisWeight), cellHeight = 42.dp)
                columns.forEach { label ->
                    MandalaCell(
                        text = label,
                        modifier = Modifier.weight(cellWeight),
                        isHeader = true,
                        cellHeight = 42.dp,
                        headerFontSize = 11
                    )
                }
            }
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MandalaCell(text = row.toString(), modifier = Modifier.weight(axisWeight), isHeader = true)
                    columns.forEach {
                        MandalaCell(text = "", modifier = Modifier.weight(cellWeight))
                    }
                }
            }
        }
        MandalaTrajectory(
            trajectory = trajectory,
            levels = rows,
            evaluations = rows,
            headerHeight = 42.dp,
            modifier = Modifier.fillMaxSize()
        )
    }
    if (trajectory.isEmpty()) {
        Text(
            text = "曼荼羅に表示できる開始時の状態記録はまだありません。",
            color = TextHint,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun MandalaTrajectory(
    trajectory: List<MandalaTrajectoryPoint>,
    levels: List<Int>,
    evaluations: List<Int>,
    headerHeight: androidx.compose.ui.unit.Dp = 26.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (trajectory.isEmpty()) return@Canvas

        val spacing = 6.dp.toPx()
        val headerHeightPx = headerHeight.toPx()
        val rowHeight = 52.dp.toPx()
        val availableWidth = size.width - spacing * levels.size
        val weightUnit = availableWidth / (levels.size + 0.5f)
        val axisWidth = weightUnit * 0.5f
        val cellWidth = weightUnit
        val occurrenceCount = mutableMapOf<Pair<Int, Int>, Int>()
        val positions = trajectory.mapNotNull { point ->
            val column = levels.indexOf(point.cell.first)
            val row = evaluations.indexOf(point.cell.second)
            if (column < 0 || row < 0) return@mapNotNull null

            val occurrence = occurrenceCount.getOrDefault(point.cell, 0)
            occurrenceCount[point.cell] = occurrence + 1
            val offsets = listOf(
                0f to 0f,
                -14.dp.toPx() to 0f,
                14.dp.toPx() to 0f,
                0f to -14.dp.toPx(),
                0f to 14.dp.toPx(),
                -12.dp.toPx() to -12.dp.toPx(),
                12.dp.toPx() to -12.dp.toPx(),
                -12.dp.toPx() to 12.dp.toPx(),
                12.dp.toPx() to 12.dp.toPx()
            )
            val offset = offsets[occurrence % offsets.size]
            point to androidx.compose.ui.geometry.Offset(
                x = axisWidth + spacing + column * (cellWidth + spacing) + cellWidth / 2f + offset.first,
                y = headerHeightPx + row * rowHeight + rowHeight / 2f + offset.second
            )
        }

        positions.zipWithNext().forEach { (from, to) ->
            drawLine(
                color = MandalaTrajectoryLineColor,
                start = from.second,
                end = to.second,
                strokeWidth = 1.25.dp.toPx()
            )
        }
        positions.forEach { (point, center) ->
            drawCircle(
                color = MandalaTrajectoryPointColor,
                radius = if (point.isLatest) 7.dp.toPx() else 6.dp.toPx(),
                center = center
            )
            if (point.isLatest) {
                drawCircle(
                    color = MandalaTrajectoryPointColor,
                    radius = 10.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun MandalaCell(
    text: String,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false,
    textColor: Color = TextPrimary,
    cellHeight: androidx.compose.ui.unit.Dp = 52.dp,
    headerFontSize: Int = 16
) {
    Card(
        modifier = modifier.height(cellHeight),
        colors = CardDefaults.cardColors(
            containerColor = if (isHeader) Color(0xFFEAF3EF) else Color.White
        ),
        border = BorderStroke(1.dp, CalmSubButtonBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (text.isNotEmpty()) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = if (isHeader) headerFontSize.sp else 18.sp,
                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                    lineHeight = if (isHeader) 18.sp else 22.sp
                )
            }
        }
    }
}

@Composable
private fun SavedRecordListItem(
    record: SavedExerciseRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDelete,
                    modifier = Modifier.heightIn(min = 36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "削除",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
                Text(
                    text = recordListDate(record.date),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )
            }
            Text(
                text = "プログラム名：${record.programCategory}",
                color = TextPrimary,
                lineHeight = 22.sp
            )
            Text(
                text = "${recordListLevel(record.level)}　${recordEvaluationMark(record.selfEvaluation)}",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
            Text(
                text = "自己評価：${record.selfEvaluation}",
                color = TextPrimary,
                lineHeight = 22.sp
            )
            Text(
                text = "詳細を見る ＞",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(vertical = 8.dp),
                color = PastelGreenButton,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun SavedRecordDetailScreen(
    record: SavedExerciseRecord,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    ScreenTitle("記録詳細")
    SectionCard(
        title = "${record.level} ${record.exerciseName}",
        body = "実施日：${record.date}\n\n" +
            "実施時刻：${record.time}\n\n" +
            "プログラム名：${record.programCategory}\n\n" +
            "レベル：${record.level}\n\n" +
            "運動名：${record.exerciseName}\n\n" +
            "実施内容：${record.content}\n\n" +
            "実施記録：${record.count.ifBlank { "記録なし" }}"
    )
    SectionCard(
        title = "開始時の状態",
        body = if (record.hasStartCondition) {
            "【現在の状態】\n\n" +
                "痛み：${record.startPain}\n" +
                "疲労：${record.startFatigue}\n" +
                "睡眠：${sleepDisplayText(record.startSleep)}\n" +
                "呼吸状態：${record.startBreathing}\n" +
                "昨日との比較：${record.startComparison}\n\n" +
                "【動ける範囲】\n\n" +
                "起き上がる：${record.startGetUp}\n" +
                "座る：${record.startSit}\n" +
                "立つ：${record.startStand}\n" +
                "屋内を歩く：${record.startIndoorWalk}\n" +
                "屋外へ出る：${record.startOutdoorWalk}"
        } else {
            "開始時の状態：記録なし"
        }
    )
    SectionCard(
        title = "開始前メモ",
        body = record.preExerciseMemo.ifBlank { "メモなし" }
    )
    SectionCard(
        title = "運動後の自己評価",
        body = record.selfEvaluation.ifBlank { "記録なし" }
    )
    SectionCard(
        title = "安全確認",
        body = "めまい：${record.dizziness.ifBlank { "記録なし" }}\n" +
            "息苦しさ：${record.breathlessness.ifBlank { "記録なし" }}\n" +
            "強い痛み：${record.strongPain.ifBlank { "記録なし" }}\n" +
            "転倒しそうな感じ：${record.fallRisk.ifBlank { "記録なし" }}"
    )
    OutlinedButton(
        onClick = onBack,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text("一覧に戻る")
    }
}

@Composable
private fun BasicInfoScreen(
    dateText: String,
    timeText: String,
    memo: String,
    onMemoChange: (String) -> Unit,
    onNext: () -> Unit
) {
    ScreenTitle("基本情報")
    Label("日付")
    ValueBox(dateText)
    Label("時間")
    ValueBox(timeText)
    Label("開始前メモ")
    Text(
        text = "※個人情報保護のため、氏名・住所・電話番号・病院名など、個人を特定できる情報は入力しないでください。",
        color = TextHint,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    Text(
        text = "今日の体調や気になることがあれば、活動を始める前に記録してください。",
        color = TextPrimary,
        lineHeight = 22.sp
    )
    OutlinedTextField(
        value = memo,
        onValueChange = onMemoChange,
        modifier = Modifier.fillMaxWidth(),
        minLines = 4,
        colors = appTextFieldColors(),
        placeholder = {
            Text(
                text = "例：昨日よく眠れなかった。朝から首が重い。外出後に疲れが残っている。",
                color = TextHint
            )
        }
    )
    PrimaryButton(text = "次へ", onClick = onNext)
}

@Composable
private fun SelfCheckScreen(
    state: SelfCheckState,
    onStateChange: (SelfCheckState) -> Unit,
    onNext: () -> Unit
) {
    ScreenTitle("現在の状態")
    Text(
        text = "これは点数をつける画面ではありません。今日の状態を確認し、安全な次の一歩を決めるための入力です。",
        color = TextPrimary,
        lineHeight = 22.sp
    )

    ChoiceField("痛み", PainChoices, state.pain) { onStateChange(state.copy(pain = it)) }
    ChoiceField("疲労", FatigueChoices, state.fatigue) { onStateChange(state.copy(fatigue = it)) }
    ChoiceField("睡眠", SleepChoices, state.sleep) { onStateChange(state.copy(sleep = it)) }
    ChoiceField("呼吸状態", BreathingChoices, state.breathing) { onStateChange(state.copy(breathing = it)) }
    ChoiceField("昨日との比較", ComparisonChoices, state.comparison) { onStateChange(state.copy(comparison = it)) }

    PrimaryButton(text = "動ける範囲へ", onClick = onNext)
}

@Composable
private fun AbilityRangeScreen(
    state: SelfCheckState,
    onStateChange: (SelfCheckState) -> Unit,
    onBack: () -> Unit,
    onEvaluate: () -> Unit
) {
    BackHandler(onBack = onBack)

    ScreenTitle("動ける範囲")
    Text(
        text = "無理をせず、今日安全にできる範囲を選んでください。",
        color = TextPrimary,
        lineHeight = 22.sp
    )

    ChoiceField("起き上がる", AbilityChoices, state.getUp) { onStateChange(state.copy(getUp = it)) }
    ChoiceField("座る", AbilityChoices, state.sit) { onStateChange(state.copy(sit = it)) }
    ChoiceField("立つ", AbilityChoices, state.stand) { onStateChange(state.copy(stand = it)) }
    ChoiceField("屋内を歩く", AbilityChoices, state.indoorWalk) { onStateChange(state.copy(indoorWalk = it)) }
    ChoiceField("屋外へ出る", AbilityChoices, state.outdoor) { onStateChange(state.copy(outdoor = it)) }

    Text(
        text = "できることを増やすためではなく、今日の安全な位置を確認します。",
        color = TextHint,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    PrimaryButton(text = "現在位置を確認する", onClick = onEvaluate)
}

@Composable
private fun ResultScreen(
    level: ProgramLevel,
    onNext: () -> Unit,
    onRecheck: () -> Unit
) {
    ScreenTitle("プログラム評価")
    MessageCard(
        text = level.name,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
    MessageCard(text = level.message)
    PrimaryButton(text = "次へ", onClick = onNext)
    OutlinedButton(
        onClick = onRecheck,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text("自己評価を見直す")
    }
}

@Composable
private fun BeforeStartScreen(
    onStartProgram1: () -> Unit,
    onStartProgram2: () -> Unit
) {
    ScreenTitle("開始前確認")
    MessageCard(
        text = "すべての運動は深呼吸から始まります。\n\n深呼吸は準備運動ではありません。\n\n今日の身体の状態を確認するための最初の評価です。"
    )
    ProgramSelectionButton(
        title = "プログラム1　基本回復プログラム",
        subtitle = "深呼吸から始める",
        onClick = onStartProgram1
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "すでに起き上がり・座位・立位が安定し、歩行練習を始められる場合に選んでください。",
        color = TextHint,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    ProgramSelectionButton(
        title = "プログラム2　歩行プログラム",
        subtitle = "歩行練習へ進む",
        onClick = onStartProgram2
    )
}

@Composable
private fun DeepBreathingLevel1Screen(
    dateText: String,
    timeText: String,
    preExerciseMemo: String,
    startState: SelfCheckState,
    recordProgramName: String,
    onBackToTop: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var breathlessness by remember { mutableStateOf(0) }
    var dizziness by remember { mutableStateOf(0) }
    var chestPain by remember { mutableStateOf(0) }
    var afterPractice by remember { mutableStateOf(0) }
    var recordMessage by remember { mutableStateOf("") }

    ScreenTitle("レベル1 深呼吸の練習")
    SectionCard(
        title = "目的",
        body = "正しい深呼吸を身につけることです。\n\n回数を増やすことが目的ではありません。\n\nゆっくりと深く呼吸する感覚を身につけることが目的です。"
    )
    SectionCard(
        title = "練習方法",
        body = "① 楽な姿勢になります。\n\n② 鼻からゆっくり息を吸います。\n\n③ 胸とお腹が自然に動くことを確認します。\n\n④ ゆっくり最後まで息を吐きます。\n\n⑤ これを3回繰り返します。"
    )
    SectionCard(
        title = "実施期間",
        body = "最初の3日間は、この深呼吸の練習のみを行います。\n\n身体が新しい刺激に慣れる時間を大切にしてください。\n\nもし少し悪化した場合は、4日間このレベルを続けてください。"
    )
    Label("安全確認")
    ChoiceField("息苦しさはありませんか。", SafetyCheckChoices, breathlessness) { breathlessness = it }
    ChoiceField("めまいはありませんか。", SafetyCheckChoices, dizziness) { dizziness = it }
    ChoiceField("胸の痛みはありませんか。", SafetyCheckChoices, chestPain) { chestPain = it }
    Label("練習後の状態確認")
    ChoiceField("深呼吸後の状態", AfterPracticeChoices, afterPractice) { afterPractice = it }
    SectionCard(
        title = "次のレベルへ進む条件",
        body = "深呼吸の練習を3日間続け、大きな悪化がないことです。\n\n少し悪化した場合は、4日間続けてから次へ進みます。"
    )
    RecordButton(
        onClick = {
            recordMessage = if (
                saveExerciseRecordSafely(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    program = DeepBreathingLevel1Program,
                    recordProgramName = recordProgramName,
                    startState = startState,
                    count = DeepBreathingCountChoices.first(),
                    selfEvaluation = AfterPracticeChoices[afterPractice],
                    dizziness = SafetyCheckChoices[dizziness],
                    breathlessness = SafetyCheckChoices[breathlessness],
                    strongPain = RecordNotChecked,
                    fallRisk = RecordNotChecked,
                    preExerciseMemo = preExerciseMemo
                )
            ) {
                "今日の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (recordMessage.isNotBlank()) {
        MessageCard(text = recordMessage)
        PrimaryButton(text = "最初に戻る", onClick = onBackToTop)
    }
    PrimaryButton(text = "レベル2へ進む", onClick = onNext)
}

@Composable
private fun DeepBreathingLevel2Screen(
    dateText: String,
    timeText: String,
    preExerciseMemo: String,
    startState: SelfCheckState,
    recordProgramName: String,
    onBackToTop: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var afterPractice by remember { mutableStateOf(0) }
    var recordMessage by remember { mutableStateOf("") }

    ScreenTitle("第1歩 深呼吸 レベル2")
    Text(
        text = "4日目以降",
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp
    )
    SectionCard(
        title = "開始条件",
        body = "同じ深呼吸プログラムを3日間継続し、大きな悪化がない場合に開始します。\n\n※少し悪化した場合は、4日間同じレベルを継続してから次へ進みます。"
    )
    SectionCard(
        title = "目的",
        body = "胸郭と横隔膜の柔軟性を高め、呼吸運動をさらに改善することを目的とします。"
    )
    SectionCard(
        title = "実施方法",
        body = "まず通常の深呼吸を3回行います。\n\nその後、息を十分吸ったところで、さらに少しだけ息を吸い足します（補気）。\n\n無理に大きく吸う必要はありません。\n\n胸郭と横隔膜が心地よく伸びる程度で十分です。\n\nそのまま数秒保ち、ゆっくり最後まで息を吐きます。\n\nこれを3回繰り返します。"
    )
    SectionCard(
        title = "確認項目",
        body = "・胸が広がる感じがありますか。\n\n・息苦しさはありませんか。\n\n・めまいはありませんか。\n\n・胸の痛みはありませんか。"
    )
    Label("深呼吸後の自己評価")
    ChoiceField("深呼吸後の状態", AfterPracticeChoices, afterPractice) { afterPractice = it }
    SectionCard(
        title = "注意",
        body = "胸や肩に力を入れ過ぎないでください。\n\n身体が心地よく伸びる範囲で行います。\n\n無理をしないことが最も重要です。"
    )
    RecordButton(
        onClick = {
            recordMessage = if (
                saveExerciseRecordSafely(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    program = DeepBreathingLevel2Program,
                    recordProgramName = recordProgramName,
                    startState = startState,
                    count = DeepBreathingCountChoices.first(),
                    selfEvaluation = AfterPracticeChoices[afterPractice],
                    dizziness = RecordNotChecked,
                    breathlessness = RecordNotChecked,
                    strongPain = RecordNotChecked,
                    fallRisk = RecordNotChecked,
                    preExerciseMemo = preExerciseMemo
                )
            ) {
                "今日の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (recordMessage.isNotBlank()) {
        MessageCard(text = recordMessage)
        PrimaryButton(text = "最初に戻る", onClick = onBackToTop)
    }
    PrimaryButton(text = "レベル3へ進む", onClick = onNext)
}

@Composable
private fun StandingLevel3Screen(
    dateText: String,
    timeText: String,
    preExerciseMemo: String,
    startState: SelfCheckState,
    recordProgramName: String,
    onBackToTop: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var dizziness by remember { mutableStateOf(0) }
    var breathlessness by remember { mutableStateOf(0) }
    var strongPain by remember { mutableStateOf(0) }
    var fallRisk by remember { mutableStateOf(0) }
    var afterPractice by remember { mutableStateOf(0) }
    var recordMessage by remember { mutableStateOf("") }

    ScreenTitle("レベル3 起立練習")
    SectionCard(
        title = "目的",
        body = "安全に立ち上がる動作を始めることです。\n\n立ち上がる回数を競うことが目的ではありません。\n\n身体が起立動作に慣れることを目的とします。"
    )
    SectionCard(
        title = "開始条件",
        body = "深呼吸レベル2を3日間継続し、大きな悪化がない場合に開始します。\n\n少し悪化した場合は、4日間継続してから次へ進みます。"
    )
    SectionCard(
        title = "実施方法",
        body = "① 深呼吸を3回行います。\n\n② 安定した椅子に座ります。\n\n③ 必要に応じて手すりや机を使用します。\n\n④ ゆっくり立ち上がります。\n\n⑤ 数秒間立位を保ちます。\n\n⑥ ゆっくり座ります。\n\n最初は 1回 行います。\n\n身体の状態が安定していれば、3日ごと（または4日ごと）に 1回 → 2回 → 3回 と少しずつ増やしてください。"
    )
    Label("安全確認")
    ChoiceField("めまいはありませんか。", SafetyCheckChoices, dizziness) { dizziness = it }
    ChoiceField("息苦しさはありませんか。", SafetyCheckChoices, breathlessness) { breathlessness = it }
    ChoiceField("強い痛みはありませんか。", SafetyCheckChoices, strongPain) { strongPain = it }
    ChoiceField("転倒しそうな感じはありませんか。", SafetyCheckChoices, fallRisk) { fallRisk = it }
    Label("終了後の自己評価")
    ChoiceField("起立練習後の状態", AfterPracticeChoices, afterPractice) { afterPractice = it }
    SectionCard(
        title = "次のレベルへ進む条件",
        body = "起立練習を3回、安全に行えるようになり、大きな悪化がないこと。\n\n少し悪化した場合は、4日間継続してから次へ進みます。"
    )
    RecordButton(
        onClick = {
            recordMessage = if (
                saveExerciseRecordSafely(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    program = StandingLevel3Program,
                    recordProgramName = recordProgramName,
                    startState = startState,
                    count = StandingCountChoices.first(),
                    selfEvaluation = AfterPracticeChoices[afterPractice],
                    dizziness = SafetyCheckChoices[dizziness],
                    breathlessness = SafetyCheckChoices[breathlessness],
                    strongPain = SafetyCheckChoices[strongPain],
                    fallRisk = SafetyCheckChoices[fallRisk],
                    preExerciseMemo = preExerciseMemo
                )
            ) {
                "今日の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (recordMessage.isNotBlank()) {
        MessageCard(text = recordMessage)
        PrimaryButton(text = "最初に戻る", onClick = onBackToTop)
    }
    PrimaryButton(text = "レベル3-2へ進む", onClick = onNext)
}

@Composable
private fun DeepSquatLevel32Screen(
    dateText: String,
    timeText: String,
    preExerciseMemo: String,
    startState: SelfCheckState,
    recordProgramName: String,
    onBackToTop: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var dizziness by remember { mutableStateOf(0) }
    var strongPain by remember { mutableStateOf(0) }
    var fallRisk by remember { mutableStateOf(0) }
    var heldTenSeconds by remember { mutableStateOf(0) }
    var afterPractice by remember { mutableStateOf(0) }
    var recordMessage by remember { mutableStateOf("") }

    ScreenTitle("レベル3-2 和式座り")
    Text(
        text = "Deep Squat",
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp
    )
    SectionCard(
        title = "目的",
        body = "下肢・股関節・骨盤・体幹の柔軟性と安定性を改善することを目的とします。\n\n和式座りは、日本人が昔から行ってきた自然な姿勢です。\n\n無理に深くしゃがむ必要はありません。\n\n痛みのない範囲で行ってください。"
    )
    SectionCard(
        title = "開始条件",
        body = "起立練習を安全に行えるようになり、大きな悪化がない場合に開始します。\n\n少し悪化した場合は、現在のレベルを4日間継続してから次へ進んでください。"
    )
    SectionCard(
        title = "実施方法",
        body = "① 安定した場所で行います。\n\n② 必要に応じて机や手すりを使用してください。\n\n③ ゆっくり和式座り（Deep Squat）の姿勢になります。\n\n④ 10秒間保持します。\n\n⑤ ゆっくり立ち上がります。\n\n1日1回のみ実施してください。"
    )
    Label("安全確認")
    ChoiceField("めまいはありませんか。", SafetyCheckChoices, dizziness) { dizziness = it }
    ChoiceField("強い痛みはありませんか。", SafetyCheckChoices, strongPain) { strongPain = it }
    ChoiceField("転倒しそうな感じはありませんか。", SafetyCheckChoices, fallRisk) { fallRisk = it }
    Label("記録")
    ChoiceField("10秒保持できたか", HoldTenSecondsChoices, heldTenSeconds) { heldTenSeconds = it }
    Label("終了後の自己評価")
    ChoiceField("和式座り後の状態", AfterPracticeChoices, afterPractice) { afterPractice = it }
    RecordButton(
        onClick = {
            recordMessage = if (
                saveExerciseRecordSafely(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    program = DeepSquatLevel32Program,
                    recordProgramName = recordProgramName,
                    startState = startState,
                    count = HoldTenSecondsChoices[heldTenSeconds],
                    selfEvaluation = AfterPracticeChoices[afterPractice],
                    dizziness = SafetyCheckChoices[dizziness],
                    breathlessness = RecordNotChecked,
                    strongPain = SafetyCheckChoices[strongPain],
                    fallRisk = SafetyCheckChoices[fallRisk],
                    preExerciseMemo = preExerciseMemo
                )
            ) {
                "今日の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (recordMessage.isNotBlank()) {
        MessageCard(text = recordMessage)
        PrimaryButton(text = "最初に戻る", onClick = onBackToTop)
    }
    PrimaryButton(text = "レベル4へ進む", onClick = onNext)
}

@Composable
private fun IndoorWalkingLevel4Screen(
    dateText: String,
    timeText: String,
    preExerciseMemo: String,
    startState: SelfCheckState,
    recordProgramName: String,
    onBackToTop: () -> Unit,
    onComplete: () -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var dizziness by remember { mutableStateOf(0) }
    var breathlessness by remember { mutableStateOf(0) }
    var strongPain by remember { mutableStateOf(0) }
    var fallRisk by remember { mutableStateOf(0) }
    var toilet by remember { mutableStateOf(false) }
    var washroom by remember { mutableStateOf(false) }
    var kitchen by remember { mutableStateOf(false) }
    var diningTable by remember { mutableStateOf(false) }
    var entrance by remember { mutableStateOf(false) }
    var afterPractice by remember { mutableStateOf(0) }
    var recordMessage by remember { mutableStateOf("") }

    val achievedText = listOf(
        recordCheckLine("トイレ", toilet),
        recordCheckLine("洗面所", washroom),
        recordCheckLine("台所", kitchen),
        recordCheckLine("食卓", diningTable),
        recordCheckLine("玄関", entrance)
    ).joinToString("\n")

    if (onNavigateBack != null) {
        BackHandler(onBack = onNavigateBack)
        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("開始前確認へ戻る")
        }
    }

    ScreenTitle("レベル4 屋内歩行練習")
    SectionCard(
        title = "目的",
        body = "歩くこと自体が目的ではありません。\n\n日常生活の中で必要な場所へ、安全に移動できるようになることが目的です。\n\n焦らず、一つずつ生活範囲を広げていきます。"
    )
    SectionCard(
        title = "開始条件",
        body = "起立練習を安全に行えるようになり、大きな悪化がない場合に開始します。\n\n少し悪化した場合は、現在のレベルを4日間継続してから次へ進んでください。"
    )
    SectionCard(
        title = "実施方法",
        body = "次の目標を一つずつ達成していきます。\n\n□ トイレまで歩いて行く\n\n□ 洗面所まで歩いて行く\n\n□ 台所まで歩いて行く\n\n□ 食卓まで歩いて行く\n\n□ 玄関まで歩いて行く\n\nすべてを一度に行う必要はありません。\n\nその日に達成できた目標だけで十分です。"
    )
    Label("安全確認")
    ChoiceField("めまいはありませんか。", SafetyCheckChoices, dizziness) { dizziness = it }
    ChoiceField("息苦しさはありませんか。", SafetyCheckChoices, breathlessness) { breathlessness = it }
    ChoiceField("強い痛みはありませんか。", SafetyCheckChoices, strongPain) { strongPain = it }
    ChoiceField("転倒しそうな感じはありませんか。", SafetyCheckChoices, fallRisk) { fallRisk = it }
    Label("今日達成できた場所")
    CheckItem("トイレ", toilet) { toilet = it }
    CheckItem("洗面所", washroom) { washroom = it }
    CheckItem("台所", kitchen) { kitchen = it }
    CheckItem("食卓", diningTable) { diningTable = it }
    CheckItem("玄関", entrance) { entrance = it }
    Label("終了後の自己評価")
    ChoiceField("屋内歩行練習後の状態", AfterPracticeChoices, afterPractice) { afterPractice = it }
    SectionCard(
        title = "記録",
        body = "今日到達できた場所を記録してください。\n\n達成した場所、自己評価、実施日時を保存してください。\n\n途中で症状が悪化した場合は、その時点で終了し、現在到達した場所までを記録してください。"
    )
    RecordButton(
        onClick = {
            recordMessage = if (
                saveExerciseRecordSafely(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    program = IndoorWalkingLevel4Program,
                    recordProgramName = recordProgramName,
                    startState = startState,
                    count = achievedText,
                    selfEvaluation = AfterPracticeChoices[afterPractice],
                    dizziness = SafetyCheckChoices[dizziness],
                    breathlessness = SafetyCheckChoices[breathlessness],
                    strongPain = SafetyCheckChoices[strongPain],
                    fallRisk = SafetyCheckChoices[fallRisk],
                    preExerciseMemo = preExerciseMemo
                )
            ) {
                "今日の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (recordMessage.isNotBlank()) {
        MessageCard(text = recordMessage)
        PrimaryButton(text = "最初に戻る", onClick = onBackToTop)
    }
    SectionCard(
        title = "確認",
        body = "今日の目標を達成できたことを確認してください。\n\n無理に次の目標へ進む必要はありません。\n\n生活範囲を少しずつ広げることが、このレベルの目的です。"
    )
    PrimaryButton(
        text = "家庭生活復帰プログラム修了へ進む",
        onClick = onComplete
    )
}

@Composable
private fun FamilyProgramCompletedScreen(
    onBackToTop: () -> Unit
) {
    ScreenTitle("家庭生活復帰プログラム修了")
    MessageCard(
        text = "お疲れさまでした。\n\n家庭生活復帰プログラムを修了しました。"
    )
    PrimaryButton(
        text = "家庭生活復帰プログラムを終了する",
        onClick = onBackToTop
    )
}

@Composable
private fun SectionCard(
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
            Text(
                text = body,
                color = TextPrimary,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun ScreenTitle(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp
    )
}

@Composable
private fun Label(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ValueBox(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = TextPrimary
        )
    }
}

@Composable
private fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.Black,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    cursorColor = Color.Black,
    focusedPlaceholderColor = TextHint,
    unfocusedPlaceholderColor = TextHint,
    focusedTrailingIconColor = Color.Black,
    unfocusedTrailingIconColor = Color.Black
)

@Composable
private fun ChoiceField(
    label: String,
    choices: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val spinnerTextStyle = TextStyle(
        color = Color.Black,
        fontSize = 18.sp,
        lineHeight = 27.sp
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Label(label)
        OutlinedTextField(
            value = choices[selectedIndex],
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            colors = appTextFieldColors(),
            textStyle = spinnerTextStyle,
            trailingIcon = {
                Button(onClick = { expanded = true }) {
                    Text("選択")
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            containerColor = Color.White
        ) {
            choices.forEachIndexed { index, choice ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = choice,
                            color = Color.Black,
                            fontSize = 18.sp,
                            lineHeight = 27.sp
                        )
                    },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    colors = MenuDefaults.itemColors(textColor = Color.Black)
                )
            }
        }
    }
}

@Composable
private fun MessageCard(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = TextPrimary,
            fontSize = fontSize,
            fontWeight = fontWeight,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun PainCompassGuidanceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append("現在の状態に不安を感じた場合や、\n痛み・疲労・回復状態に変化があった場合は、\n\n")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("「Pain Compass（疼痛コンパス）」")
                }
                append("\n\nで現在位置を確認してください。\n\n現在位置を確認してから、\n無理のない範囲で日常生活の回復を続けましょう。")
            },
            modifier = Modifier.padding(16.dp),
            color = TextPrimary,
            fontSize = 18.sp,
            lineHeight = 27.sp
        )
    }
}

@Composable
private fun ProgramSelectionButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun CalmNextStageButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CalmDeepTeal,
            contentColor = Color.White
        )
    ) {
        Text(text)
    }
}

@Composable
private fun CalmBackButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = BorderStroke(1.dp, CalmSubButtonBorder)
    ) {
        Text("戻る")
    }
}

@Composable
private fun RecordButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text("記録する")
    }
}

@Composable
private fun CheckItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            color = TextPrimary
        )
    }
}

private fun judgeProgramLevel(state: SelfCheckState): ProgramLevel {
    return when {
        state.getUp == 2 || state.sit == 2 -> ProgramLevel(
            "寝たきりレベル",
            "今日は横になった状態から始めます。深呼吸で身体の状態を確認し、無理に起き上がらなくて大丈夫です。"
        )

        state.stand == 2 -> ProgramLevel(
            "座位レベル",
            "今日は座位を安全な出発点にします。座った状態で身体の反応を確認しながら進めます。"
        )

        state.indoorWalk == 2 -> ProgramLevel(
            "立位レベル",
            "今日は立位を安全な出発点にします。立つ動作を急がず、短く確認しながら進めます。"
        )

        state.outdoor == 2 || hasStrongBurden(state) -> ProgramLevel(
            "屋内歩行レベル",
            "今日は屋内歩行を安全な出発点にします。屋外へ進む前に、室内で身体の反応を確認します。"
        )

        else -> ProgramLevel(
            "屋外歩行レベル",
            "今日は屋外歩行を安全な出発点にできます。最初は短く、深呼吸から始めます。"
        )
    }
}

private fun hasStrongBurden(state: SelfCheckState): Boolean {
    return state.pain >= 3 ||
        state.fatigue >= 3 ||
        state.sleep >= 3 ||
        state.breathing >= 2 ||
        state.comparison >= 3
}

private fun saveExerciseRecordSafely(
    context: Context,
    dateText: String,
    timeText: String,
    program: ExerciseProgram,
    recordProgramName: String,
    startState: SelfCheckState,
    count: String,
    selfEvaluation: String,
    dizziness: String,
    breathlessness: String,
    strongPain: String,
    fallRisk: String,
    preExerciseMemo: String = ""
): Boolean {
    return runCatching {
        saveExerciseRecord(
            context = context,
            dateText = dateText,
            timeText = timeText,
            program = program,
            recordProgramName = recordProgramName,
            startState = startState,
            count = count,
            selfEvaluation = selfEvaluation,
            dizziness = dizziness,
            breathlessness = breathlessness,
            strongPain = strongPain,
            fallRisk = fallRisk,
            preExerciseMemo = preExerciseMemo
        )
    }.isSuccess
}

private fun saveExerciseRecord(
    context: Context,
    dateText: String,
    timeText: String,
    program: ExerciseProgram,
    recordProgramName: String,
    startState: SelfCheckState,
    count: String,
    selfEvaluation: String,
    dizziness: String,
    breathlessness: String,
    strongPain: String,
    fallRisk: String,
    preExerciseMemo: String = ""
) {
    val preferences = context.getSharedPreferences(ExerciseRecordPreferencesName, Context.MODE_PRIVATE)
    val records = JSONArray(preferences.getString(ExerciseRecordListKey, "[]") ?: "[]")
    val startCondition = startConditionStorageValues(startState)
    val mobility = mobilityStorageValues(startState)
    val record = JSONObject()
        .put("date", dateText)
        .put("time", timeText)
        .put("programName", recordProgramName)
        .put("level", program.level)
        .put("content", program.content)
        .put("count", count)
        .put("selfEvaluation", selfEvaluation)
        .put("preExerciseMemo", preExerciseMemo)
        .put(
            "startCondition",
            JSONObject()
                .put("pain", startCondition.getValue("pain"))
                .put("fatigue", startCondition.getValue("fatigue"))
                .put("sleep", startCondition.getValue("sleep"))
                .put("breathing", startCondition.getValue("breathing"))
                .put("comparedWithYesterday", startCondition.getValue("comparedWithYesterday"))
        )
        .put(
            "mobility",
            JSONObject()
                .put("getUp", mobility.getValue("getUp"))
                .put("sit", mobility.getValue("sit"))
                .put("stand", mobility.getValue("stand"))
                .put("indoorWalk", mobility.getValue("indoorWalk"))
                .put("outdoorWalk", mobility.getValue("outdoorWalk"))
        )
        .put(
            "safety",
            JSONObject()
                .put("dizziness", dizziness)
                .put("breathlessness", breathlessness)
                .put("strongPain", strongPain)
                .put("fallRisk", fallRisk)
        )
    records.put(record)
    preferences.edit().putString(ExerciseRecordListKey, records.toString()).apply()
}

internal fun startConditionStorageValues(state: SelfCheckState): Map<String, String> = linkedMapOf(
    "pain" to PainChoices[state.pain],
    "fatigue" to FatigueChoices[state.fatigue],
    "sleep" to SleepStorageChoices[state.sleep],
    "breathing" to BreathingChoices[state.breathing],
    "comparedWithYesterday" to ComparisonChoices[state.comparison]
)

internal fun mobilityStorageValues(state: SelfCheckState): Map<String, String> = linkedMapOf(
    "getUp" to AbilityChoices[state.getUp],
    "sit" to AbilityChoices[state.sit],
    "stand" to AbilityChoices[state.stand],
    "indoorWalk" to AbilityChoices[state.indoorWalk],
    "outdoorWalk" to AbilityChoices[state.outdoor]
)

private fun recordCheckLine(label: String, checked: Boolean): String {
    return "${if (checked) "✓" else "□"} $label"
}

private fun recordListDate(date: String): String {
    val match = Regex("""(\d{4})年(\d{1,2})月(\d{1,2})日""").matchEntire(date)
    return if (match != null) {
        val (year, month, day) = match.destructured
        "$year/${month.padStart(2, '0')}/${day.padStart(2, '0')}"
    } else {
        date
    }
}

private fun recordListLevel(level: String): String {
    return level.replace("レベル", "L")
}

private fun recordEvaluationMark(value: String): String {
    return value.takeIf { it.isNotBlank() }?.substringBefore(" ") ?: "-"
}

private fun sleepDisplayText(storedValue: String): String {
    return if (storedValue == "少し眠れた") {
        "少し目が覚めることがあった"
    } else {
        storedValue
    }
}

internal fun exerciseNameForRecord(programName: String, level: String): String {
    if (programName != BasicRecoveryProgramName && programName != WalkingProgramName) {
        return programName
    }
    return when (level) {
        "レベル1" -> "深呼吸の練習"
        "レベル2" -> "深呼吸"
        "レベル3" -> "起立練習"
        "レベル3-2" -> "和式座り（Deep Squat）"
        "レベル4" -> "屋内歩行練習"
        else -> programName
    }
}

private fun recoveryMandalaTrajectory(
    records: List<SavedExerciseRecord>
): List<MandalaTrajectoryPoint> {
    return buildMandalaTrajectory(
        statesNewestFirst = records.map { record ->
            MandalaStartState(
                fatigue = record.startFatigue,
                getUp = record.startGetUp,
                sit = record.startSit,
                stand = record.startStand,
                indoorWalk = record.startIndoorWalk,
                outdoorWalk = record.startOutdoorWalk
            )
        },
    )
}

private fun shouldShowSocialActivityGuidanceForRecords(
    records: List<SavedExerciseRecord>
): Boolean {
    return shouldShowSocialActivityGuidance(
        statesNewestFirst = records.map { record ->
            MandalaStartState(
                fatigue = record.startFatigue,
                getUp = record.startGetUp,
                sit = record.startSit,
                stand = record.startStand,
                indoorWalk = record.startIndoorWalk,
                outdoorWalk = record.startOutdoorWalk
            )
        }
    )
}

internal fun shouldShowSocialActivityGuidance(
    statesNewestFirst: List<MandalaStartState>
): Boolean {
    val latestThree = statesNewestFirst.take(3)
    return latestThree.size == 3 && latestThree.all { state ->
        mobilityMandalaColumn(state) == 5 &&
            fatigueMandalaRow(state.fatigue) in 1..2
    }
}

internal fun mobilityMandalaColumn(state: MandalaStartState): Int? {
    val mobility = listOf(state.getUp, state.sit, state.stand, state.indoorWalk, state.outdoorWalk)
    if (mobility.any { it.isBlank() }) return null
    if (mobility.any { it !in AbilityChoices }) return null

    return mobility.indexOfLast { it == "できる" || it == "少しならできる" }
        .takeIf { it >= 0 }
        ?.plus(1)
        ?: 1
}

internal fun fatigueMandalaRow(fatigue: String): Int? = when (fatigue) {
    "無し" -> 1
    "軽い" -> 2
    "少しある" -> 3
    "強い" -> 4
    "かなり強い", "休息が必要" -> 5
    else -> null
}

internal fun buildMandalaTrajectory(
    statesNewestFirst: List<MandalaStartState>
): List<MandalaTrajectoryPoint> {
    return statesNewestFirst
        .mapNotNull { state ->
            val column = mobilityMandalaColumn(state) ?: return@mapNotNull null
            val row = fatigueMandalaRow(state.fatigue) ?: return@mapNotNull null
            column to row
        }
        .mapIndexed { index, cell ->
            MandalaTrajectoryPoint(cell = cell, isLatest = index == 0)
        }
        .asReversed()
}

private fun deleteExerciseRecordSafely(context: Context, storageIndex: Int): Boolean {
    return runCatching {
        val preferences = context.getSharedPreferences(ExerciseRecordPreferencesName, Context.MODE_PRIVATE)
        val records = JSONArray(preferences.getString(ExerciseRecordListKey, "[]") ?: "[]")
        if (storageIndex !in 0 until records.length()) return@runCatching false
        records.remove(storageIndex)
        preferences.edit().putString(ExerciseRecordListKey, records.toString()).apply()
        true
    }.getOrDefault(false)
}

internal fun shouldShowDeleteAllRecords(recordCount: Int): Boolean = recordCount > 0

private fun deleteAllExerciseRecordsSafely(context: Context): Boolean {
    return runCatching {
        val preferences = context.getSharedPreferences(ExerciseRecordPreferencesName, Context.MODE_PRIVATE)
        preferences.edit().remove(ExerciseRecordListKey).commit()
    }.getOrDefault(false)
}

private fun loadExerciseRecordsSafely(context: Context): List<SavedExerciseRecord> {
    return runCatching {
        val preferences = context.getSharedPreferences(ExerciseRecordPreferencesName, Context.MODE_PRIVATE)
        val records = JSONArray(preferences.getString(ExerciseRecordListKey, "[]") ?: "[]")
        List(records.length()) { index ->
            val record = records.getJSONObject(index)
            val safety = record.optJSONObject("safety")
            val startCondition = record.optJSONObject("startCondition")
            val mobility = record.optJSONObject("mobility")
            SavedExerciseRecord(
                storageIndex = index,
                date = record.optString("date"),
                time = record.optString("time"),
                level = record.optString("level"),
                programName = record.optString("programName"),
                content = record.optString("content"),
                count = record.optString("count"),
                selfEvaluation = record.optString("selfEvaluation"),
                preExerciseMemo = record.optString("preExerciseMemo"),
                dizziness = safety?.optString("dizziness").orEmpty(),
                breathlessness = safety?.optString("breathlessness").orEmpty(),
                strongPain = safety?.optString("strongPain").orEmpty(),
                fallRisk = safety?.optString("fallRisk").orEmpty(),
                startPain = startCondition?.optString("pain").orEmpty(),
                startFatigue = startCondition?.optString("fatigue").orEmpty(),
                startSleep = startCondition?.optString("sleep").orEmpty(),
                startBreathing = startCondition?.optString("breathing").orEmpty(),
                startComparison = startCondition?.optString("comparedWithYesterday").orEmpty(),
                startGetUp = mobility?.optString("getUp").orEmpty(),
                startSit = mobility?.optString("sit").orEmpty(),
                startStand = mobility?.optString("stand").orEmpty(),
                startIndoorWalk = mobility?.optString("indoorWalk").orEmpty(),
                startOutdoorWalk = mobility?.optString("outdoorWalk").orEmpty(),
                nextDayWorse = record.optString("nextDayWorse"),
                nextCriteria = record.optString("nextCriteria"),
                backCriteria = record.optString("backCriteria")
            )
        }.asReversed()
    }.getOrDefault(emptyList())
}

private val PainChoices = listOf("痛くない", "軽い", "少しある", "強い", "かなり強い", "動くのがつらい")
private val FatigueChoices = listOf("無し", "軽い", "少しある", "強い", "かなり強い", "休息が必要")
private val SleepChoices = listOf("眠れた", "少し目が覚めることがあった", "眠りが浅い", "ほとんど眠れない")
private val SleepStorageChoices = listOf("眠れた", "少し眠れた", "眠りが浅い", "ほとんど眠れない")
private val BreathingChoices = listOf("落ち着いている", "少し浅い", "苦しさがある", "苦しさが強い")
private val ComparisonChoices = listOf("昨日より良い", "少し良い", "変わらない", "少し悪い", "かなり悪い")
private val AbilityChoices = listOf("できる", "少しならできる", "今日は難しい")
private val SafetyCheckChoices = listOf("ない", "少しある", "ある")
private val AfterPracticeChoices = listOf("① 楽になった", "② 変わらない", "③ 少しつらい", "④ 思ったよりつらかった", "⑤ 動けなくなった")
private val HoldTenSecondsChoices = listOf("できた", "少しできた", "今日は難しい")
private const val RecordNotChecked = "未確認"
private val DeepBreathingCountChoices = listOf("3回")
private val StandingCountChoices = listOf("1回", "2回", "3回")

private val DeepBreathingLevel1Program = ExerciseProgram(
    programName = "深呼吸の練習",
    level = "レベル1",
    content = "鼻からゆっくり息を吸い、胸とお腹の自然な動きを確認しながら、ゆっくり最後まで息を吐きます。",
    countChoices = DeepBreathingCountChoices
)

private val DeepBreathingLevel2Program = ExerciseProgram(
    programName = "深呼吸",
    level = "レベル2",
    content = "通常の深呼吸を3回行った後、息を十分吸ったところで少しだけ吸い足し、数秒保ってからゆっくり吐きます。",
    countChoices = DeepBreathingCountChoices
)

private val StandingLevel3Program = ExerciseProgram(
    programName = "起立練習",
    level = "レベル3",
    content = "安定した椅子から必要に応じて手すりや机を使用し、ゆっくり立ち上がって数秒保ち、ゆっくり座ります。",
    countChoices = StandingCountChoices
)

private val DeepSquatLevel32Program = ExerciseProgram(
    programName = "和式座り（Deep Squat）",
    level = "レベル3-2",
    content = "安定した場所で必要に応じて机や手すりを使用し、ゆっくり和式座りの姿勢になって10秒間保持します。",
    countChoices = HoldTenSecondsChoices
)

private val IndoorWalkingLevel4Program = ExerciseProgram(
    programName = "屋内歩行練習",
    level = "レベル4",
    content = "日常生活の中で必要な場所へ、安全に移動できるように、達成できる場所を一つずつ広げます。",
    countChoices = listOf("トイレ", "洗面所", "台所", "食卓", "玄関")
)

private const val ExerciseRecordPreferencesName = "exercise_records"
private const val ExerciseRecordListKey = "records"
internal const val BasicRecoveryProgramName = "基本回復プログラム"
internal const val WalkingProgramName = "歩行プログラム"

private val ScreenBackground = Color(0xFFF7FAF8)
private val TextPrimary = Color.Black
private val TextHint = Color(0xFF555555)
private val PastelGreenButton = Color(0xFF4CAF72)
private val MandalaPurpleButton = Color(0xFF5E548E)
private val MandalaTrajectoryPointColor = Color(0xFF37474F)
private val MandalaTrajectoryLineColor = Color(0xFF90A4AE)
private val CalmDeepTeal = Color(0xFF1F6B5C)
private val CalmSubButtonBorder = Color(0xFFE0E3E1)
@Preview(showBackground = true)
@Composable
private fun DailyLivingCompassPreview() {
    DailyLivingCompassTheme {
        DailyLivingCompassApp()
    }
}
