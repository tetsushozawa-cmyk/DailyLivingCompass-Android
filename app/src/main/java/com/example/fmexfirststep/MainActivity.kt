package com.example.fmexfirststep

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.example.fmexfirststep.ui.theme.FMEXFirstStepTheme
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
            FMEXFirstStepTheme {
                FibromyalgiaFirstStepApp()
            }
        }
    }
}

private enum class AppStep {
    Top,
    SavedRecords,
    RecoveryProgress,
    RecoveryMandala,
    BasicInfo,
    SelfCheck,
    Result,
    BeforeStart,
    DeepBreathingLevel1,
    DeepBreathingLevel2,
    StandingLevel3,
    DeepSquatLevel32,
    IndoorWalkingLevel4,
    FamilyProgramCompleted,
    SocialRehabilitationTop,
    SocialRehabilitationRecords,
    SocialRehabilitationLevel1,
    SocialRehabilitationLevel2,
    SocialRehabilitationLevel2Solo,
    SocialRehabilitationLevel2Ride,
    SocialRehabilitationLevel3,
    SocialRehabilitationLevel3Car,
    SocialRehabilitationLevel4,
    SocialRehabilitationLevel5,
    SocialRehabilitationLevel6
}

private data class SelfCheckState(
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
    val nextDayWorse: String,
    val nextCriteria: String,
    val backCriteria: String
) {
    val programCategory: String
        get() = if (level.startsWith("社会復帰編") || programName == "社会復帰プログラム") {
            "社会復帰プログラム"
        } else {
            "家庭生活復帰プログラム"
    }
}

private data class MandalaMarker(
    val symbol: String,
    val color: Color,
    val fontSize: Int,
    val backgroundColor: Color? = null,
    val isHistoryDot: Boolean = false
)

@Composable
private fun FibromyalgiaFirstStepApp() {
    var step by remember { mutableStateOf(AppStep.Top) }
    var checkState by remember { mutableStateOf(SelfCheckState()) }
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var basicMemo by remember { mutableStateOf("") }
    var level by remember { mutableStateOf<ProgramLevel?>(null) }
    var socialPreviewOnly by remember { mutableStateOf(false) }
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
                        onStartSocialProgram = { step = AppStep.SocialRehabilitationTop },
                        onShowRecords = { step = AppStep.SavedRecords },
                        onShowRecoveryProgress = { step = AppStep.RecoveryProgress },
                        onShowRecoveryMandala = { step = AppStep.RecoveryMandala }
                    )

                    AppStep.SavedRecords -> SavedRecordsScreen(
                        onBack = { step = AppStep.Top },
                        onlySocialRehabilitation = false,
                        onResetScroll = { scrollResetKey++ }
                    )

                    AppStep.RecoveryProgress -> RecoveryProgressScreen(
                        onBack = { step = AppStep.Top }
                    )

                    AppStep.RecoveryMandala -> RecoveryMandalaScreen(
                        onBack = { step = AppStep.Top }
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
                        onStartProgram = { step = AppStep.DeepBreathingLevel1 }
                    )

                    AppStep.DeepBreathingLevel1 -> DeepBreathingLevel1Screen(
                        preExerciseMemo = basicMemo,
                        onBackToTop = { step = AppStep.Top },
                        onNext = { step = AppStep.DeepBreathingLevel2 }
                    )

                    AppStep.DeepBreathingLevel2 -> DeepBreathingLevel2Screen(
                        preExerciseMemo = basicMemo,
                        onBackToTop = { step = AppStep.Top },
                        onNext = { step = AppStep.StandingLevel3 }
                    )

                    AppStep.StandingLevel3 -> StandingLevel3Screen(
                        preExerciseMemo = basicMemo,
                        onBackToTop = { step = AppStep.Top },
                        onNext = { step = AppStep.DeepSquatLevel32 }
                    )

                    AppStep.DeepSquatLevel32 -> DeepSquatLevel32Screen(
                        preExerciseMemo = basicMemo,
                        onBackToTop = { step = AppStep.Top },
                        onNext = { step = AppStep.IndoorWalkingLevel4 }
                    )

                    AppStep.IndoorWalkingLevel4 -> IndoorWalkingLevel4Screen(
                        preExerciseMemo = basicMemo,
                        onBackToTop = { step = AppStep.Top },
                        onComplete = { step = AppStep.FamilyProgramCompleted }
                    )

                    AppStep.FamilyProgramCompleted -> FamilyProgramCompletedScreen(
                        onBackToTop = { step = AppStep.Top },
                        onStartSocialProgram = { step = AppStep.SocialRehabilitationTop }
                    )

                    AppStep.SocialRehabilitationTop -> SocialRehabilitationTopScreen(
                        onStartLevel1 = {
                            socialPreviewOnly = false
                            step = AppStep.SocialRehabilitationLevel1
                        },
                        onStartLevel2 = {
                            socialPreviewOnly = false
                            step = AppStep.SocialRehabilitationLevel2
                        },
                        onStartLevel3 = {
                            socialPreviewOnly = false
                            step = AppStep.SocialRehabilitationLevel3
                        },
                        onStartLevel3Car = {
                            socialPreviewOnly = false
                            step = AppStep.SocialRehabilitationLevel3Car
                        },
                        onStartLevel4 = {
                            socialPreviewOnly = false
                            step = AppStep.SocialRehabilitationLevel4
                        },
                        onStartLevel5 = {
                            socialPreviewOnly = false
                            step = AppStep.SocialRehabilitationLevel5
                        },
                        onStartLevel6 = {
                            socialPreviewOnly = false
                            step = AppStep.SocialRehabilitationLevel6
                        },
                        onShowRecords = { step = AppStep.SocialRehabilitationRecords },
                        onBackToTop = { step = AppStep.Top }
                    )

                    AppStep.SocialRehabilitationRecords -> SavedRecordsScreen(
                        onBack = { step = AppStep.SocialRehabilitationTop },
                        onlySocialRehabilitation = true,
                        onResetScroll = { scrollResetKey++ }
                    )

                    AppStep.SocialRehabilitationLevel1 -> SocialRehabilitationLevel1Screen(
                        onBack = { step = AppStep.SocialRehabilitationTop },
                        onNextLevel = { conditionMet ->
                            socialPreviewOnly = !conditionMet
                            step = AppStep.SocialRehabilitationLevel2
                        }
                    )

                    AppStep.SocialRehabilitationLevel2 -> SocialRehabilitationLevel2Screen(
                        previewOnly = socialPreviewOnly,
                        onSolo = { step = AppStep.SocialRehabilitationLevel2Solo },
                        onRide = { step = AppStep.SocialRehabilitationLevel2Ride },
                        onBack = { step = AppStep.SocialRehabilitationTop },
                        onNextLevel = { conditionMet ->
                            socialPreviewOnly = !conditionMet
                            step = AppStep.SocialRehabilitationLevel3
                        }
                    )

                    AppStep.SocialRehabilitationLevel2Solo -> SocialRehabilitationLevel2SoloScreen(
                        onBack = { step = AppStep.SocialRehabilitationTop },
                        onNextLevel3 = { step = AppStep.SocialRehabilitationLevel3 },
                        onNextLevel3Car = { step = AppStep.SocialRehabilitationLevel3Car },
                        onNextLevel4 = { step = AppStep.SocialRehabilitationLevel4 },
                        onNextLevel5 = { step = AppStep.SocialRehabilitationLevel5 }
                    )

                    AppStep.SocialRehabilitationLevel2Ride -> SocialRehabilitationLevel2RideScreen(
                        onBack = { step = AppStep.SocialRehabilitationTop },
                        onNextLevel3 = { step = AppStep.SocialRehabilitationLevel3 },
                        onNextLevel3Car = { step = AppStep.SocialRehabilitationLevel3Car },
                        onNextLevel4 = { step = AppStep.SocialRehabilitationLevel4 },
                        onNextLevel5 = { step = AppStep.SocialRehabilitationLevel5 }
                    )

                    AppStep.SocialRehabilitationLevel3 -> SocialRehabilitationLevel3Screen(
                        previewOnly = socialPreviewOnly,
                        onBack = { step = AppStep.SocialRehabilitationTop },
                        onNextLevel4 = { step = AppStep.SocialRehabilitationLevel4 }
                    )

                    AppStep.SocialRehabilitationLevel3Car -> SocialRehabilitationLevel3CarScreen(
                        onBack = { step = AppStep.SocialRehabilitationTop },
                        onNextLevel4 = { step = AppStep.SocialRehabilitationLevel4 }
                    )

                    AppStep.SocialRehabilitationLevel4 -> SocialRehabilitationLevel4Screen(
                        onBack = { step = AppStep.SocialRehabilitationTop },
                        onNextLevel5 = { step = AppStep.SocialRehabilitationLevel5 }
                    )

                    AppStep.SocialRehabilitationLevel5 -> SocialRehabilitationLevel5Screen(
                        onBack = { step = AppStep.SocialRehabilitationTop },
                        onNextLevel6 = { step = AppStep.SocialRehabilitationLevel6 }
                    )

                    AppStep.SocialRehabilitationLevel6 -> SocialRehabilitationLevel6Screen(
                        onBack = { step = AppStep.SocialRehabilitationTop }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopScreen(
    onStart: () -> Unit,
    onStartSocialProgram: () -> Unit,
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
        text = "生活と社会への回復を、一歩ずつ見える形にするプログラム",
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
        Button(
            onClick = onStartSocialProgram,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
        ) {
            Text("社会復帰プログラム")
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
    onlySocialRehabilitation: Boolean,
    onResetScroll: () -> Unit
) {
    val context = LocalContext.current
    var allRecords by remember { mutableStateOf(loadExerciseRecordsSafely(context)) }
    val records =
        if (onlySocialRehabilitation) {
            allRecords.filter { it.programCategory == "社会復帰プログラム" }
        } else {
            allRecords
        }
    var selectedRecord by remember { mutableStateOf<SavedExerciseRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<SavedExerciseRecord?>(null) }

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
        ScreenTitle(if (onlySocialRehabilitation) "社会復帰プログラムの記録" else "記録を見る")
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
private fun RecoveryMandalaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val records = remember { loadExerciseRecordsSafely(context) }
    val evaluations = listOf(1, 2, 3, 4, 5)
    val initialLevels = listOf(2, 1)
    val latterLevels = listOf(7, 6, 5, 4, 3)
    val initialMandalaMarkers = remember(records) {
        recoveryMandalaMarkers(records, initialLevels, evaluations)
    }
    val latterMandalaMarkers = remember(records) {
        recoveryMandalaMarkers(records, latterLevels, evaluations)
    }

    ScreenTitle("回復曼荼羅")
    RecoveryMandalaGrid(
        title = "社会復帰 初期曼荼羅",
        subtitle = "レベル1〜2",
        levels = initialLevels,
        evaluations = evaluations,
        mandalaMarkers = initialMandalaMarkers
    )
    MessageCard(text = "詳細は疼痛コンパスを使用してください。")
    RecoveryMandalaGrid(
        title = "社会復帰 後半曼荼羅",
        subtitle = "レベル3〜7",
        levels = latterLevels,
        evaluations = evaluations,
        mandalaMarkers = latterMandalaMarkers
    )
    RecoveryMandalaLegend()
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
    title: String,
    subtitle: String,
    levels: List<Int>,
    evaluations: List<Int>,
    mandalaMarkers: Map<Pair<Int, Int>, List<MandalaMarker>>
) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    Text(
        text = subtitle,
        color = TextPrimary,
        lineHeight = 22.sp
    )
    val axisWeight = 0.5f
    val cellWeight = 1f
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MandalaCell(text = "", modifier = Modifier.weight(axisWeight), cellHeight = 26.dp)
        levels.forEach { level ->
            MandalaCell(text = level.toString(), modifier = Modifier.weight(cellWeight), isHeader = true, cellHeight = 26.dp)
        }
    }
    evaluations.forEach { evaluation ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MandalaCell(text = evaluation.toString(), modifier = Modifier.weight(axisWeight), isHeader = true)
            levels.forEach { level ->
                val cell = level to evaluation
                MandalaCell(
                    text = "",
                    modifier = Modifier.weight(cellWeight),
                    markers = compactMandalaMarkers(mandalaMarkers[cell].orEmpty())
                )
            }
        }
    }
}

@Composable
private fun RecoveryMandalaLegend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Label("色の説明")
            LegendItem(symbol = "★", color = Color(0xFFFFC107), text = "最新の記録", fontSize = 18)
            LegendItem(symbol = "★", color = Color.Black, text = "2番目に新しい記録", fontSize = 18)
            LegendItem(symbol = "☆", color = Color.White, text = "3番目に新しい記録", fontSize = 18, backgroundColor = Color(0xFF7A7A7A))
            LegendItem(symbol = "●", color = MandalaOneWeekColor, text = "過去1週間")
            LegendItem(symbol = "●", color = MandalaTwoWeeksColor, text = "過去2週間")
            LegendItem(symbol = "●", color = MandalaFourWeeksColor, text = "過去4週間")
            LegendItem(symbol = "●", color = MandalaEightWeeksColor, text = "過去8週間")
            LegendItem(symbol = "●", color = MandalaTwelveWeeksColor, text = "過去12週間")
        }
    }
}

@Composable
private fun LegendItem(
    symbol: String,
    color: Color,
    text: String,
    fontSize: Int = 16,
    backgroundColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = symbol,
            color = color,
            modifier = if (backgroundColor != null) {
                Modifier
                    .background(backgroundColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 2.dp)
            } else {
                Modifier
            },
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp
        )
        Text(
            text = text,
            color = TextPrimary,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun MandalaCell(
    text: String,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false,
    textColor: Color = TextPrimary,
    markers: List<MandalaMarker> = emptyList(),
    cellHeight: androidx.compose.ui.unit.Dp = 52.dp
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
            if (markers.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    markers.forEach { marker ->
                        Text(
                            text = marker.symbol,
                            color = marker.color,
                            modifier = if (marker.backgroundColor != null) {
                                Modifier
                                    .background(marker.backgroundColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 1.dp)
                            } else {
                                Modifier
                            },
                            fontSize = marker.fontSize.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = marker.fontSize.sp
                        )
                    }
                }
            }
            if (text.isNotEmpty()) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = if (isHeader) 16.sp else 18.sp,
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
                text = "${recordListLevel(record.level)}　${recordEvaluationMark(record.selfEvaluation)}→${recordEvaluationMark(record.nextDayWorse)}",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
            Text(
                text = "自己評価：${record.selfEvaluation}\n翌日：${record.nextDayWorse.ifBlank { "記録なし" }}",
                color = TextPrimary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun SavedRecordDetailScreen(
    record: SavedExerciseRecord,
    onBack: () -> Unit
) {
    ScreenTitle("記録詳細")
    SectionCard(
        title = "${record.level} ${record.programName}",
        body = "実施日：${record.date}\n\n実施時刻：${record.time}\n\nプログラム名：${record.programCategory}\n\nレベル：${record.level}\n\n運動名：${record.programName}\n\n実施内容：${record.content}\n\n実施記録：${record.count.ifBlank { "記録なし" }}\n\n自己評価：${record.selfEvaluation}\n\n翌日の悪化：${record.nextDayWorse.ifBlank { "記録なし" }}\n\n次の段階へ進む目安：${record.nextCriteria.ifBlank { "記録なし" }}\n\n一つ前の段階へ戻る目安：${record.backCriteria.ifBlank { "記録なし" }}\n\n開始前メモ：${record.preExerciseMemo.ifBlank { "メモなし" }}\n\n安全確認：\nめまい：${record.dizziness.ifBlank { "記録なし" }}\n息苦しさ：${record.breathlessness.ifBlank { "記録なし" }}\n強い痛み：${record.strongPain.ifBlank { "記録なし" }}\n転倒しそうな感じ：${record.fallRisk.ifBlank { "記録なし" }}"
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
        text = "運動を始める前に、覚えていることや気になることがあれば記録してください。",
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
    onEvaluate: () -> Unit
) {
    ScreenTitle("自己評価")
    Text(
        text = "これは点数をつける画面ではありません。今日の現在位置を確認し、安全な次の一歩を決めるための入力です。",
        color = TextPrimary,
        lineHeight = 22.sp
    )

    ChoiceField("痛み", PainChoices, state.pain) { onStateChange(state.copy(pain = it)) }
    ChoiceField("疲労", FatigueChoices, state.fatigue) { onStateChange(state.copy(fatigue = it)) }
    ChoiceField("睡眠", SleepChoices, state.sleep) { onStateChange(state.copy(sleep = it)) }
    ChoiceField("呼吸状態", BreathingChoices, state.breathing) { onStateChange(state.copy(breathing = it)) }
    ChoiceField("昨日との比較", ComparisonChoices, state.comparison) { onStateChange(state.copy(comparison = it)) }
    ChoiceField("起き上がれるか", AbilityChoices, state.getUp) { onStateChange(state.copy(getUp = it)) }
    ChoiceField("座れるか", AbilityChoices, state.sit) { onStateChange(state.copy(sit = it)) }
    ChoiceField("立てるか", AbilityChoices, state.stand) { onStateChange(state.copy(stand = it)) }
    ChoiceField("屋内を歩けるか", AbilityChoices, state.indoorWalk) { onStateChange(state.copy(indoorWalk = it)) }
    ChoiceField("屋外へ出られるか", AbilityChoices, state.outdoor) { onStateChange(state.copy(outdoor = it)) }

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
private fun BeforeStartScreen(onStartProgram: () -> Unit) {
    ScreenTitle("開始前確認")
    MessageCard(
        text = "すべての運動は深呼吸から始まります。\n\n深呼吸は準備運動ではありません。\n\n今日の身体の状態を確認するための最初の評価です。"
    )
    PrimaryButton(
        text = "運動プログラム開始",
        onClick = onStartProgram
    )
}

@Composable
private fun DeepBreathingLevel1Screen(
    preExerciseMemo: String,
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
                    program = DeepBreathingLevel1Program,
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
    preExerciseMemo: String,
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
                    program = DeepBreathingLevel2Program,
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
    preExerciseMemo: String,
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
                    program = StandingLevel3Program,
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
    preExerciseMemo: String,
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
                    program = DeepSquatLevel32Program,
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
    preExerciseMemo: String,
    onBackToTop: () -> Unit,
    onComplete: () -> Unit
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
                    program = IndoorWalkingLevel4Program,
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
    onBackToTop: () -> Unit,
    onStartSocialProgram: () -> Unit
) {
    ScreenTitle("家庭生活復帰プログラム修了")
    MessageCard(
        text = "お疲れさまでした。\n\n家庭生活復帰プログラムを修了しました。\n\n次の目標に進む方は、\n\n社会復帰プログラムを開始してください。"
    )
    PrimaryButton(
        text = "家庭生活復帰プログラムを終了する",
        onClick = onBackToTop
    )
    PrimaryButton(
        text = "社会復帰プログラムを開始する",
        onClick = onStartSocialProgram
    )
}

@Composable
private fun SocialRehabilitationTopScreen(
    onStartLevel1: () -> Unit,
    onStartLevel2: () -> Unit,
    onStartLevel3: () -> Unit,
    onStartLevel3Car: () -> Unit,
    onStartLevel4: () -> Unit,
    onStartLevel5: () -> Unit,
    onStartLevel6: () -> Unit,
    onShowRecords: () -> Unit,
    onBackToTop: () -> Unit
) {
    ScreenTitle("社会復帰プログラム")
    MessageCard(
        text = "家庭生活復帰プログラムを修了された方、\n\nまたは医師から勧められた方が対象です。\n\n現在の状態に合わせて、取り組むレベルを選んでください。"
    )
    CalmNextStageButton(
        text = "レベル1　屋外歩行",
        onClick = onStartLevel1
    )
    CalmNextStageButton(
        text = "レベル2　買い物練習",
        onClick = onStartLevel2
    )
    CalmNextStageButton(
        text = "レベル3-A　公共交通機関通勤予行",
        onClick = onStartLevel3
    )
    CalmNextStageButton(
        text = "レベル3-B　自家用車通勤予行",
        onClick = onStartLevel3Car
    )
    CalmNextStageButton(
        text = "レベル4　職場滞在予行",
        onClick = onStartLevel4
    )
    CalmNextStageButton(
        text = "レベル5　短時間勤務予行",
        onClick = onStartLevel5
    )
    CalmNextStageButton(
        text = "レベル6　社会復帰後の注意事項",
        onClick = onStartLevel6
    )
    OutlinedButton(
        onClick = onShowRecords,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text("記録を見る")
    }
    OutlinedButton(
        onClick = onBackToTop,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text("トップ画面へ戻る")
    }
}

@Composable
private fun SocialRehabilitationLevel1Screen(
    onBack: () -> Unit,
    onNextLevel: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val now = remember { Date() }
    val dateText = remember { SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now) }
    val timeText = remember { SimpleDateFormat("HH:mm", Locale.JAPAN).format(now) }
    var timeOfDay by remember { mutableStateOf(0) }
    var targetPoint by remember { mutableStateOf(0) }
    var reachedPoint by remember { mutableStateOf(0) }
    var walkedTenSeconds by remember { mutableStateOf(false) }
    var walkedTwentySeconds by remember { mutableStateOf(false) }
    var walkedThirtySeconds by remember { mutableStateOf(false) }
    var restedThirtySeconds by remember { mutableStateOf(false) }
    var restedOneMinute by remember { mutableStateOf(false) }
    var returnedSafely by remember { mutableStateOf(false) }
    var selfEvaluation by remember { mutableStateOf(0) }
    var nextDayWorse by remember { mutableStateOf(0) }
    var savedMessage by remember { mutableStateOf("") }

    ScreenTitle("社会復帰編 レベル1")
    Text(
        text = "5分以内の屋外歩行",
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    SectionCard(
        title = "目的",
        body = "自宅から5分以内で行ける場所まで、休みながら歩く練習を行います。\n\n目的地に到達することが目的ではありません。\n\n「ここまでなら安全に行って帰って来られる」という範囲を確認することが目的です。"
    )
    SectionCard(
        title = "実施方法",
        body = "① 自宅から5分以内で行ける目標地点を決めます。\n\n② 最初は10秒歩きます。\n\n③ その後、30秒休みます。\n\n④ 次に20秒歩きます。\n\n⑤ その後、30秒休みます。\n\n⑥ 次に30秒歩きます。\n\n⑦ その後、30秒休みます。\n\n⑧ 以後は、\n\n30秒歩行 → 30秒休み\n\nのペースで、目標地点を目指します。\n\n⑨ 「今日はここまで」と思った場所で止まります。\n\n⑩ その場で1分休みます。\n\n⑪ 同じ道を歩いて自宅へ戻ります。\n\n⑫ 午前1回、午後1回までとします。"
    )
    SectionCard(
        title = "重要ルール",
        body = "・無理に目的地まで行かないでください。\n\n・途中でつらくなったら、その場所を今日の到達点とします。\n\n・到達点で1分休んでから引き返します。\n\n・帰宅後に無理に追加して歩かないでください。\n\n・翌日に悪化していないことを確認してから次へ進みます。"
    )
    Label("記録項目")
    Label("日付")
    ValueBox(dateText)
    Label("時刻")
    ValueBox(timeText)
    ChoiceField("午前／午後", SocialTimeOfDayChoices, timeOfDay) { timeOfDay = it }
    ChoiceField("目標地点", SocialWalkingMinuteChoices, targetPoint) { targetPoint = it }
    ChoiceField("今日の到達地点", SocialWalkingMinuteChoices, reachedPoint) { reachedPoint = it }
    CheckItem("10秒歩行を行えた", walkedTenSeconds) { walkedTenSeconds = it }
    CheckItem("20秒歩行を行えた", walkedTwentySeconds) { walkedTwentySeconds = it }
    CheckItem("30秒歩行を行えた", walkedThirtySeconds) { walkedThirtySeconds = it }
    CheckItem("30秒休憩を守れた", restedThirtySeconds) { restedThirtySeconds = it }
    CheckItem("到達点で1分休憩できた", restedOneMinute) { restedOneMinute = it }
    CheckItem("安全に帰宅できた", returnedSafely) { returnedSafely = it }
    ChoiceField("実施後の自己評価", SocialSelfEvaluationChoices, selfEvaluation) { selfEvaluation = it }
    ChoiceField("翌日の悪化", NextDayWorseChoices, nextDayWorse) { nextDayWorse = it }
    SectionCard(
        title = "次へ進む条件",
        body = "午前1回、午後1回の歩行を行っても、\n\n翌日に大きな悪化がないこと。"
    )
    RecordButton(
        onClick = {
            savedMessage = if (
                saveSimpleSocialRehabilitationRecordSafely(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    level = "社会復帰編 レベル1",
                    content = "5分以内の屋外歩行",
                    count = "午前／午後：${SocialTimeOfDayChoices[timeOfDay]}\n\n" +
                        "目標地点：${SocialWalkingMinuteChoices[targetPoint]}\n\n" +
                        "今日の到達地点：${SocialWalkingMinuteChoices[reachedPoint]}\n\n" +
                        recordCheckLine("10秒歩行を行えた", walkedTenSeconds) + "\n\n" +
                        recordCheckLine("20秒歩行を行えた", walkedTwentySeconds) + "\n\n" +
                        recordCheckLine("30秒歩行を行えた", walkedThirtySeconds) + "\n\n" +
                        recordCheckLine("30秒休憩を守れた", restedThirtySeconds) + "\n\n" +
                        recordCheckLine("到達点で1分休憩できた", restedOneMinute) + "\n\n" +
                        recordCheckLine("安全に帰宅できた", returnedSafely),
                    selfEvaluation = SocialSelfEvaluationChoices[selfEvaluation],
                    nextDayWorse = NextDayWorseChoices[nextDayWorse]
                )
            ) {
                "レベル1の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (savedMessage.isNotBlank()) {
        MessageCard(text = savedMessage)
    }
    CalmNextStageButton(
        text = "次のレベルへ",
        onClick = {
            onNextLevel(returnedSafely && nextDayWorse == 0)
        }
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
private fun SocialRehabilitationLevel2Screen(
    previewOnly: Boolean,
    onSolo: () -> Unit,
    onRide: () -> Unit,
    onBack: () -> Unit,
    onNextLevel: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val now = remember { Date() }
    val dateText = remember { SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now) }
    val timeText = remember { SimpleDateFormat("HH:mm", Locale.JAPAN).format(now) }
    var selfEvaluation by remember { mutableStateOf(0) }
    var nextDayWorse by remember { mutableStateOf(0) }
    var savedMessage by remember { mutableStateOf("") }

    ScreenTitle("社会復帰編 レベル2")
    if (previewOnly) {
        MessageCard(text = "まだ次へ進む条件は満たしていません。\n内容の確認のみです。")
    }
    Text(
        text = "買い物練習",
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    SectionCard(
        title = "目的",
        body = "買い物に行くこと自体が目的ではありません。\n\n買い物中に無理をせず、休みながら安全に行動できる範囲を確認することが目的です。\n\nどちらで買い物を行いますか？"
    )
    Label("記録項目")
    Label("日付")
    ValueBox(dateText)
    Label("時刻")
    ValueBox(timeText)
    ChoiceField("実施後の自己評価", SocialSelfEvaluationChoices, selfEvaluation) { selfEvaluation = it }
    ChoiceField("翌日の評価", NextDayWorseChoices, nextDayWorse) { nextDayWorse = it }
    RecordButton(
        onClick = {
            savedMessage = if (
                saveSimpleSocialRehabilitationRecordSafely(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    level = "社会復帰編 レベル2",
                    content = "買い物練習",
                    count = "買い物練習の方法選択前の記録",
                    selfEvaluation = SocialSelfEvaluationChoices[selfEvaluation],
                    nextDayWorse = NextDayWorseChoices[nextDayWorse]
                )
            ) {
                "レベル2の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (savedMessage.isNotBlank()) {
        MessageCard(text = savedMessage)
    }
    PrimaryButton(
        text = "一人でスーパーへ行く",
        onClick = onSolo
    )
    PrimaryButton(
        text = "スーパーまで送ってもらう",
        onClick = onRide
    )
    CalmNextStageButton(
        text = "次のレベルへ",
        onClick = {
            onNextLevel(nextDayWorse == 0)
        }
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
private fun SocialRehabilitationLevel2SoloScreen(
    onBack: () -> Unit,
    onNextLevel3: () -> Unit,
    onNextLevel3Car: () -> Unit,
    onNextLevel4: () -> Unit,
    onNextLevel5: () -> Unit
) {
    val context = LocalContext.current
    val now = remember { Date() }
    val dateText = remember { SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now) }
    val timeText = remember { SimpleDateFormat("HH:mm", Locale.JAPAN).format(now) }
    var keptActivityAndRest by remember { mutableStateOf(false) }
    var returnedSafely by remember { mutableStateOf(false) }
    var almostExtended by remember { mutableStateOf(false) }
    var avoidedExtraAction by remember { mutableStateOf(false) }
    var continuedThreeDayCycle by remember { mutableStateOf(false) }
    var sleptAfterShopping by remember { mutableStateOf(false) }
    var exceededTimeGreatly by remember { mutableStateOf(false) }
    var strongFatigueAfterReturning by remember { mutableStateOf(false) }
    var selfEvaluation by remember { mutableStateOf(0) }
    var nextDayWorse by remember { mutableStateOf(0) }
    var savedMessage by remember { mutableStateOf("") }

    ScreenTitle("社会復帰編 レベル2")
    Text(
        text = "一人でスーパーへ行く",
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    SectionCard(
        title = "目的",
        body = "買い物に行くこと自体が目的ではありません。\n\n買い物中に無理をせず、休みながら安全に行動できる範囲を確認することが目的です。\n\n買い物は楽しくなりやすく、やり過ぎて失敗することがあります。\n\nそのため、時間と休憩を守ることを最優先にしてください。"
    )
    SectionCard(
        title = "実施方法",
        body = "1. 社会復帰編レベル1の歩行パターンでスーパーへ向かいます。\n\n2. 店に入ったら、まず30秒休みます。\n\n3. 30秒だけ買い物をします。\n\n4. その後、30秒休みます。\n\n5. 以後は、\n\n30秒買い物 → 30秒休み\n\nを繰り返します。\n\n6. 店内での買い物時間は、合計5分を超えないようにしてください。\n\n7. 買い物が終わったら、\n\n30秒歩行 → 30秒休み\n\nの歩行パターンで帰宅します。"
    )
    SectionCard(
        title = "重要ルール",
        body = "・30秒歩行・30秒休憩でスーパーへ向かいます。\n\n・店内での買い物時間は5分以内です。\n\n・帰りも30秒歩行・30秒休憩を守ります。\n\n・楽しくなっても延長しないでください。\n\n・帰宅できる体力を必ず残してください。"
    )

    Label("記録項目")
    Label("日付")
    ValueBox(dateText)
    Label("時刻")
    ValueBox(timeText)
    Label("実施方法")
    ValueBox("一人でスーパーへ行く")
    Label("店内での買い物時間")
    ValueBox("5分以内")
    CheckItem("30秒活動と30秒休憩を守れたか", keptActivityAndRest) { keptActivityAndRest = it }
    CheckItem("安全に帰宅できたか", returnedSafely) { returnedSafely = it }
    CheckItem("楽しくなって延長しそうになったか", almostExtended) { almostExtended = it }
    CheckItem("買い物後に無理な追加行動をしなかった", avoidedExtraAction) { avoidedExtraAction = it }
    CheckItem("3日サイクルで安定して確認できた", continuedThreeDayCycle) { continuedThreeDayCycle = it }
    CheckItem("買い物後に寝込んだ", sleptAfterShopping) { sleptAfterShopping = it }
    CheckItem("予定時間を大きく超えた", exceededTimeGreatly) { exceededTimeGreatly = it }
    CheckItem("帰宅後に強い疲労が出た", strongFatigueAfterReturning) { strongFatigueAfterReturning = it }
    ChoiceField("実施後の自己評価", SocialSelfEvaluationChoices, selfEvaluation) { selfEvaluation = it }
    ChoiceField("翌日の悪化", NextDayWorseChoices, nextDayWorse) { nextDayWorse = it }
    SectionCard(
        title = "次の段階へ進む目安",
        body = "□ 買い物中に30秒活動・30秒休憩を続けられた\n\n□ 買い物時間を守れた\n\n□ 安全に帰宅できた\n\n□ 翌日に大きな悪化がなかった\n\nすべてを確認できたら、次の段階へ進むことを考えましょう。"
    )
    SectionCard(
        title = "一つ前の段階へ戻る目安",
        body = "□ 翌日に明らかな悪化があった\n\n□ 買い物後に寝込んだ\n\n□ 安全に帰宅できなかった\n\n□ 楽しくなって予定時間を大きく超えてしまった\n\n一つでも当てはまる場合は、一つ前の段階へ戻ることを考えましょう。"
    )
    MessageCard(
        text = "このプログラムは合格・不合格を判定するものではありません。\n\n身体の状態を確認しながら、安全に次の段階へ進むための目安として使ってください。"
    )
    NextDayPainCompassGuide()
    RecordButton(
        onClick = {
            savedMessage = if (
                saveSimpleSocialRehabilitationRecordSafely(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    level = "社会復帰編 レベル2",
                    content = "買い物練習：一人でスーパーへ行く",
                    count = "実施方法：一人でスーパーへ行く\n\n" +
                        "店内での買い物時間：5分以内\n\n" +
                        recordCheckLine("30秒活動と30秒休憩を守れたか", keptActivityAndRest) + "\n\n" +
                        recordCheckLine("安全に帰宅できたか", returnedSafely) + "\n\n" +
                        recordCheckLine("楽しくなって延長しそうになったか", almostExtended) + "\n\n" +
                        recordCheckLine("買い物後に無理な追加行動をしなかった", avoidedExtraAction) + "\n\n" +
                        recordCheckLine("3日サイクルで安定して確認できた", continuedThreeDayCycle) + "\n\n" +
                        recordCheckLine("買い物後に寝込んだ", sleptAfterShopping) + "\n\n" +
                        recordCheckLine("予定時間を大きく超えた", exceededTimeGreatly) + "\n\n" +
                        recordCheckLine("帰宅後に強い疲労が出た", strongFatigueAfterReturning),
                    selfEvaluation = SocialSelfEvaluationChoices[selfEvaluation],
                    nextDayWorse = NextDayWorseChoices[nextDayWorse]
                )
            ) {
                "買い物練習（一人で行く）の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (savedMessage.isNotBlank()) {
        MessageCard(text = savedMessage)
    }
    Level2EndButtons(
        onNextLevel3 = onNextLevel3,
        onNextLevel3Car = onNextLevel3Car,
        onNextLevel4 = onNextLevel4,
        onNextLevel5 = onNextLevel5,
        onBack = onBack
    )
}

@Composable
private fun SocialRehabilitationLevel2RideScreen(
    onBack: () -> Unit,
    onNextLevel3: () -> Unit,
    onNextLevel3Car: () -> Unit,
    onNextLevel4: () -> Unit,
    onNextLevel5: () -> Unit
) {
    val context = LocalContext.current
    val now = remember { Date() }
    val dateText = remember { SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now) }
    val timeText = remember { SimpleDateFormat("HH:mm", Locale.JAPAN).format(now) }
    var keptActivityAndRest by remember { mutableStateOf(false) }
    var usedCart by remember { mutableStateOf(false) }
    var returnedSafely by remember { mutableStateOf(false) }
    var almostExtended by remember { mutableStateOf(false) }
    var avoidedExtraAction by remember { mutableStateOf(false) }
    var continuedThreeDayCycle by remember { mutableStateOf(false) }
    var sleptAfterShopping by remember { mutableStateOf(false) }
    var exceededTimeGreatly by remember { mutableStateOf(false) }
    var strongFatigueAfterReturning by remember { mutableStateOf(false) }
    var selfEvaluation by remember { mutableStateOf(0) }
    var nextDayWorse by remember { mutableStateOf(0) }
    var savedMessage by remember { mutableStateOf("") }

    ScreenTitle("社会復帰編 レベル2")
    Text(
        text = "スーパーまで送ってもらう",
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    SectionCard(
        title = "目的",
        body = "買い物に行くこと自体が目的ではありません。\n\n買い物中に無理をせず、休みながら安全に行動できる範囲を確認することが目的です。\n\n買い物は楽しくなりやすく、やり過ぎて失敗することがあります。\n\nそのため、時間と休憩を守ることを最優先にしてください。"
    )
    SectionCard(
        title = "実施方法",
        body = "1. スーパーまで家族や知人に送ってもらいます。\n\n2. 店に入ったら、カートにつかまります。\n\n3. カートにつかまりながら、30秒動きます。\n\n4. その後、30秒休みます。\n\n5. 以後は、\n\n30秒動く → 30秒休む\n\nを繰り返します。\n\n6. 店内での買い物時間は、合計10分を超えないようにしてください。"
    )
    SectionCard(
        title = "重要ルール",
        body = "・カートにつかまりながら行います。\n\n・30秒活動・30秒休憩を守ります。\n\n・店内での買い物時間は10分以内です。\n\n・楽に感じても延長しないでください。\n\n・楽しくなってやり過ぎることが一番の注意点です。"
    )
    Label("記録項目")
    Label("日付")
    ValueBox(dateText)
    Label("時刻")
    ValueBox(timeText)
    Label("実施方法")
    ValueBox("スーパーまで送ってもらう")
    Label("店内での買い物時間")
    ValueBox("10分以内")
    CheckItem("30秒活動と30秒休憩を守れたか", keptActivityAndRest) { keptActivityAndRest = it }
    CheckItem("カートを使用したか", usedCart) { usedCart = it }
    CheckItem("安全に帰宅できたか", returnedSafely) { returnedSafely = it }
    CheckItem("楽しくなって延長しそうになったか", almostExtended) { almostExtended = it }
    CheckItem("買い物後に無理な追加行動をしなかった", avoidedExtraAction) { avoidedExtraAction = it }
    CheckItem("3日サイクルで安定して確認できた", continuedThreeDayCycle) { continuedThreeDayCycle = it }
    CheckItem("買い物後に寝込んだ", sleptAfterShopping) { sleptAfterShopping = it }
    CheckItem("予定時間を大きく超えた", exceededTimeGreatly) { exceededTimeGreatly = it }
    CheckItem("帰宅後に強い疲労が出た", strongFatigueAfterReturning) { strongFatigueAfterReturning = it }
    ChoiceField("実施後の自己評価", SocialSelfEvaluationChoices, selfEvaluation) { selfEvaluation = it }
    ChoiceField("翌日の悪化", NextDayWorseChoices, nextDayWorse) { nextDayWorse = it }
    SectionCard(
        title = "次の段階へ進む目安",
        body = "□ 買い物中に30秒活動・30秒休憩を続けられた\n\n□ 買い物時間を守れた\n\n□ 安全に帰宅できた\n\n□ 翌日に大きな悪化がなかった\n\nすべてを確認できたら、次の段階へ進むことを考えましょう。"
    )
    SectionCard(
        title = "一つ前の段階へ戻る目安",
        body = "□ 翌日に明らかな悪化があった\n\n□ 買い物後に寝込んだ\n\n□ 安全に帰宅できなかった\n\n□ 楽しくなって予定時間を大きく超えてしまった\n\n一つでも当てはまる場合は、一つ前の段階へ戻ることを考えましょう。"
    )
    MessageCard(
        text = "このプログラムは合格・不合格を判定するものではありません。\n\n身体の状態を確認しながら、安全に次の段階へ進むための目安として使ってください。"
    )
    NextDayPainCompassGuide()
    RecordButton(
        onClick = {
            savedMessage = if (
                saveSimpleSocialRehabilitationRecordSafely(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    level = "社会復帰編 レベル2",
                    content = "買い物練習：スーパーまで送ってもらう",
                    count = "実施方法：スーパーまで送ってもらう\n\n" +
                        "店内での買い物時間：10分以内\n\n" +
                        recordCheckLine("30秒活動と30秒休憩を守れたか", keptActivityAndRest) + "\n\n" +
                        recordCheckLine("カートを使用したか", usedCart) + "\n\n" +
                        recordCheckLine("安全に帰宅できたか", returnedSafely) + "\n\n" +
                        recordCheckLine("楽しくなって延長しそうになったか", almostExtended) + "\n\n" +
                        recordCheckLine("買い物後に無理な追加行動をしなかった", avoidedExtraAction) + "\n\n" +
                        recordCheckLine("3日サイクルで安定して確認できた", continuedThreeDayCycle) + "\n\n" +
                        recordCheckLine("買い物後に寝込んだ", sleptAfterShopping) + "\n\n" +
                        recordCheckLine("予定時間を大きく超えた", exceededTimeGreatly) + "\n\n" +
                        recordCheckLine("帰宅後に強い疲労が出た", strongFatigueAfterReturning),
                    selfEvaluation = SocialSelfEvaluationChoices[selfEvaluation],
                    nextDayWorse = NextDayWorseChoices[nextDayWorse]
                )
            ) {
                "買い物練習（送ってもらう）の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (savedMessage.isNotBlank()) {
        MessageCard(text = savedMessage)
    }
    Level2EndButtons(
        onNextLevel3 = onNextLevel3,
        onNextLevel3Car = onNextLevel3Car,
        onNextLevel4 = onNextLevel4,
        onNextLevel5 = onNextLevel5,
        onBack = onBack
    )
}

@Composable
private fun Level2EndButtons(
    onNextLevel3: () -> Unit,
    onNextLevel3Car: () -> Unit,
    onNextLevel4: () -> Unit,
    onNextLevel5: () -> Unit,
    onBack: () -> Unit
) {
    CalmNextStageButton(
        text = "レベル3-A　公共交通機関通勤予行",
        onClick = onNextLevel3
    )
    CalmNextStageButton(
        text = "レベル3-B　自家用車通勤予行",
        onClick = onNextLevel3Car
    )
    CalmNextStageButton(
        text = "レベル4　職場滞在予行",
        onClick = onNextLevel4
    )
    CalmNextStageButton(
        text = "レベル5　短時間勤務予行",
        onClick = onNextLevel5
    )
    CalmBackButton(onClick = onBack)
}

@Composable
private fun NextDayPainCompassGuide() {
    var message by remember { mutableStateOf("") }

    SectionCard(
        title = "翌日記録の案内",
        body = "買い物や外出は、身体への負荷が大きい活動です。\n\n当日の状態だけではなく、翌日の状態を確認することが大切です。\n\n明日、疼痛コンパスに戻って、痛み・疲労・回復の状態を記録してください。\n\n翌日の記録が、次の段階へ進むか、同じ段階を続けるか、一つ前の段階へ戻るかを判断する大切な情報になります。"
    )
    PrimaryButton(
        text = "疼痛コンパスに戻る",
        onClick = {
            message = "明日、疼痛コンパスで翌日の状態を記録してください。"
        }
    )
    if (message.isNotBlank()) {
        MessageCard(text = message)
    }
}

@Composable
private fun SocialRehabilitationLevel3Screen(
    previewOnly: Boolean,
    onBack: () -> Unit,
    onNextLevel4: () -> Unit
) {
    val context = LocalContext.current
    var checkedCompass by remember { mutableStateOf(false) }
    var sleptEnough by remember { mutableStateOf(false) }
    var avoidedOverwork by remember { mutableStateOf(false) }
    var usedTransit by remember { mutableStateOf(false) }
    var returnedAsPlanned by remember { mutableStateOf(false) }
    var noStrongFatigue by remember { mutableStateOf(false) }
    var noStrongPain by remember { mutableStateOf(false) }
    var tookBreaks by remember { mutableStateOf(false) }
    var selfEvaluation by remember { mutableStateOf(0) }
    var nextDayWorse by remember { mutableStateOf(0) }
    var savedMessage by remember { mutableStateOf("") }

    ScreenTitle("社会復帰編 レベル3-A")
    if (previewOnly) {
        MessageCard(text = "まだ次へ進む条件は満たしていません。\n内容の確認のみです。")
    }
    Text(
        text = "公共交通機関通勤予行",
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    SectionCard(
        title = "目的",
        body = "職場付近まで行って帰ることを目的とします。\n\n仕事を始めることが目的ではありません。\n\n通勤による身体への負荷を確認し、安全に帰宅できることを目的とします。\n\n翌日の状態まで確認して、この活動量が現在の身体に適しているかを判断します。"
    )
    SectionCard(
        title = "対象",
        body = "・電車通勤\n\n・バス通勤\n\n・徒歩を含む公共交通機関で通勤する方"
    )
    Label("前日")
    CheckItem("疼痛コンパスで現在の状態を確認する", checkedCompass) { checkedCompass = it }
    CheckItem("睡眠を十分にとる", sleptEnough) { sleptEnough = it }
    CheckItem("無理な活動を控える", avoidedOverwork) { avoidedOverwork = it }
    SectionCard(
        title = "当日",
        body = "① 自宅からバス停または駅まで歩く\n\n② ベンチなどがあれば30秒休む\n\n③ バスまたは電車に乗る\n\n④ 目的地まで移動する\n\n⑤ 到着後は30秒休む\n\n⑥ 職場または職場周辺まで歩く\n\n⑦ 30分以内の見学または滞在を行う\n\n⑧ 無理をせず帰路につく\n\n⑨ 帰宅後は十分休む"
    )
    SectionCard(
        title = "重要",
        body = "今日は仕事をする日ではありません。\n\n「安全に行って、安全に帰って来られるか」を確認する日です。\n\n途中で体調が悪くなった場合は、その場で終了し、帰宅を優先してください。"
    )
    Label("記録")
    CheckItem("バス・電車を利用できた", usedTransit) { usedTransit = it }
    CheckItem("予定どおり帰宅できた", returnedAsPlanned) { returnedAsPlanned = it }
    CheckItem("強い疲労はなかった", noStrongFatigue) { noStrongFatigue = it }
    CheckItem("強い痛みはなかった", noStrongPain) { noStrongPain = it }
    CheckItem("途中で休憩をとれた", tookBreaks) { tookBreaks = it }
    ChoiceField("実施後の自己評価", SocialSelfEvaluationChoices, selfEvaluation) { selfEvaluation = it }
    ChoiceField("翌日の評価", NextDayWorseChoices, nextDayWorse) { nextDayWorse = it }
    SectionCard(
        title = "翌日",
        body = "翌日は疼痛コンパスに戻り、\n\n痛み・疲労・回復状態を記録してください。\n\nこの記録が、次の段階へ進むかどうかを判断する大切な情報になります。"
    )
    SectionCard(
        title = "次の段階へ進む目安",
        body = "□ 公共交通機関を安全に利用できた\n\n□ 予定どおり帰宅できた\n\n□ 翌日に大きな悪化がなかった\n\n□ 活動後も回復できた\n\nすべてを確認できたら、次の段階へ進むことを考えましょう。"
    )
    SectionCard(
        title = "一つ前の段階へ戻る目安",
        body = "□ 翌日に明らかな悪化があった\n\n□ 帰宅後に寝込んだ\n\n□ 安全に帰宅できなかった\n\n□ 通勤による負荷が大きすぎた\n\n一つでも当てはまる場合は、一つ前の段階へ戻ることを考えましょう。"
    )
    RecordButton(
        onClick = {
            savedMessage = if (
                saveSocialRehabilitationLevel3RecordSafely(
                    context = context,
                    level = "レベル3-A",
                    content = "公共交通機関通勤予行",
                    checkedCompass = checkedCompass,
                    sleptEnough = sleptEnough,
                    avoidedOverwork = avoidedOverwork,
                    commuteLine = recordCheckLine("公共交通機関を利用できた", usedTransit),
                    returnedAsPlanned = returnedAsPlanned,
                    noStrongFatigue = noStrongFatigue,
                    noStrongPain = noStrongPain,
                    tookBreaks = tookBreaks,
                    selfEvaluation = SocialSelfEvaluationChoices[selfEvaluation],
                    nextDayWorse = NextDayWorseChoices[nextDayWorse],
                    nextCriteria = SocialRehabilitationLevel3TransitNextCriteria,
                    backCriteria = SocialRehabilitationLevel3TransitBackCriteria
                )
            ) {
                "レベル3-Aの記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (savedMessage.isNotBlank()) {
        MessageCard(text = savedMessage)
    }
    CalmNextStageButton(
        text = "次の段階へ進む",
        onClick = onNextLevel4
    )
    CalmBackButton(onClick = onBack)
}

@Composable
private fun SocialRehabilitationLevel3CarScreen(
    onBack: () -> Unit,
    onNextLevel4: () -> Unit
) {
    val context = LocalContext.current
    var checkedCompass by remember { mutableStateOf(false) }
    var sleptEnough by remember { mutableStateOf(false) }
    var avoidedOverwork by remember { mutableStateOf(false) }
    var usedCar by remember { mutableStateOf(false) }
    var returnedAsPlanned by remember { mutableStateOf(false) }
    var noStrongFatigue by remember { mutableStateOf(false) }
    var noStrongPain by remember { mutableStateOf(false) }
    var tookBreaks by remember { mutableStateOf(false) }
    var selfEvaluation by remember { mutableStateOf(0) }
    var nextDayWorse by remember { mutableStateOf(0) }
    var savedMessage by remember { mutableStateOf("") }

    ScreenTitle("社会復帰編 レベル3-B")
    Text(
        text = "自家用車通勤予行",
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    SectionCard(
        title = "対象",
        body = "・自家用車で通勤する方"
    )
    SectionCard(
        title = "目的",
        body = "自家用車で職場付近まで行って帰ることを目的とします。\n\n仕事を始めることが目的ではありません。\n\n運転を含む通勤による身体への負荷を確認し、安全に帰宅できることを目的とします。\n\n翌日の状態まで確認して、この活動量が現在の身体に適しているかを判断します。"
    )
    Label("前日")
    CheckItem("疼痛コンパスで現在の状態を確認する", checkedCompass) { checkedCompass = it }
    CheckItem("睡眠を十分にとる", sleptEnough) { sleptEnough = it }
    CheckItem("無理な活動を控える", avoidedOverwork) { avoidedOverwork = it }
    SectionCard(
        title = "当日",
        body = "① 自宅から職場付近まで自家用車で移動する\n\n② 到着後は30秒休む\n\n③ 職場または職場周辺まで無理のない範囲で移動する\n\n④ 30分以内の見学または滞在を行う\n\n⑤ 無理をせず帰路につく\n\n⑥ 帰宅後は十分休む"
    )
    SectionCard(
        title = "重要",
        body = "今日は仕事をする日ではありません。\n\n「安全に行って、安全に帰って来られるか」を確認する日です。\n\n途中で体調が悪くなった場合は、その場で終了し、帰宅を優先してください。"
    )
    Label("記録")
    CheckItem("自家用車で移動できた", usedCar) { usedCar = it }
    CheckItem("予定どおり帰宅できた", returnedAsPlanned) { returnedAsPlanned = it }
    CheckItem("強い疲労はなかった", noStrongFatigue) { noStrongFatigue = it }
    CheckItem("強い痛みはなかった", noStrongPain) { noStrongPain = it }
    CheckItem("途中で休憩をとれた", tookBreaks) { tookBreaks = it }
    ChoiceField("実施後の自己評価", SocialSelfEvaluationChoices, selfEvaluation) { selfEvaluation = it }
    ChoiceField("翌日の評価", NextDayWorseChoices, nextDayWorse) { nextDayWorse = it }
    SectionCard(
        title = "翌日",
        body = "翌日は疼痛コンパスに戻り、\n\n痛み・疲労・回復状態を記録してください。\n\nこの記録が、次の段階へ進むかどうかを判断する大切な情報になります。"
    )
    SectionCard(
        title = "次の段階へ進む目安",
        body = "□ 自家用車通勤の流れを安全に確認できた\n\n□ 予定どおり帰宅できた\n\n□ 翌日に大きな悪化がなかった\n\n□ 活動後も回復できた\n\nすべてを確認できたら、次の段階へ進むことを考えましょう。"
    )
    SectionCard(
        title = "一つ前の段階へ戻る目安",
        body = "□ 翌日に明らかな悪化があった\n\n□ 帰宅後に寝込んだ\n\n□ 安全に帰宅できなかった\n\n□ 運転を含む通勤による負荷が大きすぎた\n\n一つでも当てはまる場合は、一つ前の段階へ戻ることを考えましょう。"
    )
    RecordButton(
        onClick = {
            savedMessage = if (
                saveSocialRehabilitationLevel3RecordSafely(
                    context = context,
                    level = "レベル3-B",
                    content = "自家用車通勤予行",
                    checkedCompass = checkedCompass,
                    sleptEnough = sleptEnough,
                    avoidedOverwork = avoidedOverwork,
                    commuteLine = recordCheckLine("自家用車で移動できた", usedCar),
                    returnedAsPlanned = returnedAsPlanned,
                    noStrongFatigue = noStrongFatigue,
                    noStrongPain = noStrongPain,
                    tookBreaks = tookBreaks,
                    selfEvaluation = SocialSelfEvaluationChoices[selfEvaluation],
                    nextDayWorse = NextDayWorseChoices[nextDayWorse],
                    nextCriteria = SocialRehabilitationLevel3CarNextCriteria,
                    backCriteria = SocialRehabilitationLevel3CarBackCriteria
                )
            ) {
                "レベル3-Bの記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (savedMessage.isNotBlank()) {
        MessageCard(text = savedMessage)
    }
    CalmNextStageButton(
        text = "次の段階へ進む",
        onClick = onNextLevel4
    )
    CalmBackButton(onClick = onBack)
}

@Composable
private fun SocialRehabilitationLevel4Screen(
    onBack: () -> Unit,
    onNextLevel5: () -> Unit
) {
    val context = LocalContext.current
    val now = remember { Date() }
    val dateText = remember { SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now) }
    val timeText = remember { SimpleDateFormat("HH:mm", Locale.JAPAN).format(now) }
    var selfEvaluation by remember { mutableStateOf(0) }
    var nextDayWorse by remember { mutableStateOf(0) }
    var savedMessage by remember { mutableStateOf("") }

    ScreenTitle("社会復帰編 レベル4")
    Text(
        text = "職場滞在予行",
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    SectionCard(
        title = "目的",
        body = "職場に入り、短時間滞在して帰ることを目的とします。"
    )
    Label("実施日")
    ValueBox(dateText)
    Label("実施時刻")
    ValueBox(timeText)
    Label("プログラム名")
    ValueBox("社会復帰プログラム")
    Label("レベル")
    ValueBox("レベル4")
    SectionCard(
        title = "実施内容",
        body = SocialRehabilitationLevel4Content
    )
    ChoiceField("自己評価", SocialSelfEvaluationChoices, selfEvaluation) { selfEvaluation = it }
    ChoiceField("翌日の悪化", NextDayWorseChoices, nextDayWorse) { nextDayWorse = it }
    SectionCard(
        title = "次の段階へ進む目安",
        body = SocialRehabilitationLevel4NextCriteria
    )
    SectionCard(
        title = "一つ前の段階へ戻る目安",
        body = SocialRehabilitationLevel4BackCriteria
    )
    RecordButton(
        onClick = {
            savedMessage = if (
                saveSocialRehabilitationLevel4RecordSafely(
                    context = context,
                    selfEvaluation = SocialSelfEvaluationChoices[selfEvaluation],
                    nextDayWorse = NextDayWorseChoices[nextDayWorse]
                )
            ) {
                "レベル4の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (savedMessage.isNotBlank()) {
        MessageCard(text = savedMessage)
    }
    CalmNextStageButton(
        text = "レベル5へ進む",
        onClick = onNextLevel5
    )
    CalmBackButton(onClick = onBack)
}

@Composable
private fun SocialRehabilitationLevel5Screen(
    onBack: () -> Unit,
    onNextLevel6: () -> Unit
) {
    val context = LocalContext.current
    val now = remember { Date() }
    val dateText = remember { SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now) }
    val timeText = remember { SimpleDateFormat("HH:mm", Locale.JAPAN).format(now) }
    var workTime by remember { mutableStateOf(0) }
    var keptScheduledRest by remember { mutableStateOf(false) }
    var restedEveryHour by remember { mutableStateOf(false) }
    var restedPosture by remember { mutableStateOf(false) }
    var recoveredAfterRest by remember { mutableStateOf(false) }
    var suddenPain by remember { mutableStateOf(false) }
    var suddenFatigue by remember { mutableStateOf(false) }
    var brainFog by remember { mutableStateOf(false) }
    var hardToStand by remember { mutableStateOf(false) }
    var notRecoveringSitting by remember { mutableStateOf(false) }
    var notEnoughEnergyToGoHome by remember { mutableStateOf(false) }
    var leftEarly by remember { mutableStateOf(false) }
    var earlyLeaveReason by remember { mutableStateOf("") }
    var returnedSafely by remember { mutableStateOf(false) }
    var selfEvaluation by remember { mutableStateOf(0) }
    var nextDayWorse by remember { mutableStateOf(0) }
    var savedMessage by remember { mutableStateOf("") }

    ScreenTitle("社会復帰編 レベル5")
    Text(
        text = "短時間勤務予行",
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    SectionCard(
        title = "目的",
        body = "仕事を完全に再開することが目的ではありません。\n\n勤務中に休息を入れながら、安全に過ごせるかを確認します。\n\nまた、無理だと感じた時に早めに退避できることも、この段階の大切な目的です。"
    )
    SectionCard(
        title = "実施方法",
        body = "勤務中は、\n\n1時間ごとに5分間の定時休息\n\nを入れてください。\n\n休息は、症状が強くなってからではなく、\n\n症状が強くなる前に予定として入れます。"
    )
    Label("定時休息")
    CheckItem("1時間ごとに5分休めた", restedEveryHour) { restedEveryHour = it }
    CheckItem("休息中に座る、横になる、目を閉じるなどができた", restedPosture) { restedPosture = it }
    CheckItem("休息後に少し回復した", recoveredAfterRest) { recoveredAfterRest = it }
    SectionCard(
        title = "退避行動",
        body = "次のような場合は、無理をせず早退を考えてください。"
    )
    CheckItem("痛みが急に強くなった", suddenPain) { suddenPain = it }
    CheckItem("疲労が急に強くなった", suddenFatigue) { suddenFatigue = it }
    CheckItem("頭が働かなくなった", brainFog) { brainFog = it }
    CheckItem("立っていることがつらくなった", hardToStand) { hardToStand = it }
    CheckItem("座っていても回復しない", notRecoveringSitting) { notRecoveringSitting = it }
    CheckItem("帰宅する体力が残っていないと感じた", notEnoughEnergyToGoHome) { notEnoughEnergyToGoHome = it }
    SectionCard(
        title = "退避行動の説明",
        body = "早退は失敗ではありません。\n\n現在の身体にとって、その日の勤務負荷が少し大きかったという大切な情報です。\n\n無理をして最後まで続けることよりも、\n\n安全に帰宅し、翌日の状態を確認することを優先してください。"
    )
    Label("記録項目")
    Label("実施日")
    ValueBox(dateText)
    Label("実施時刻")
    ValueBox(timeText)
    ChoiceField("勤務時間", ShortWorkTimeChoices, workTime) { workTime = it }
    CheckItem("1時間ごとの5分休息を守れたか", keptScheduledRest) { keptScheduledRest = it }
    CheckItem("早退したか", leftEarly) { leftEarly = it }
    OutlinedTextField(
        value = earlyLeaveReason,
        onValueChange = { earlyLeaveReason = it },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        colors = appTextFieldColors(),
        placeholder = {
            Text(
                text = "早退した理由",
                color = TextHint
            )
        }
    )
    CheckItem("安全に帰宅できたか", returnedSafely) { returnedSafely = it }
    ChoiceField("実施後の自己評価", SocialSelfEvaluationChoices, selfEvaluation) { selfEvaluation = it }
    ChoiceField("翌日の悪化", NextDayWorseChoices, nextDayWorse) { nextDayWorse = it }
    SectionCard(
        title = "次の段階へ進む目安",
        body = "□ 1時間ごとの5分休息を守れた\n\n□ 勤務中に大きく崩れなかった\n\n□ 安全に帰宅できた\n\n□ 翌日に大きな悪化がなかった\n\nすべてを確認できたら、次の段階へ進むことを考えましょう。"
    )
    SectionCard(
        title = "一つ前の段階へ戻る目安",
        body = "□ 休息を入れても回復しなかった\n\n□ 早退が必要だった\n\n□ 帰宅後に寝込んだ\n\n□ 翌日に明らかな悪化があった\n\n一つでも当てはまる場合は、一つ前の段階へ戻ることを考えましょう。"
    )
    RecordButton(
        onClick = {
            savedMessage = if (
                saveSocialRehabilitationLevel5RecordSafely(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    workTime = ShortWorkTimeChoices[workTime],
                    restedEveryHour = restedEveryHour,
                    restedPosture = restedPosture,
                    recoveredAfterRest = recoveredAfterRest,
                    suddenPain = suddenPain,
                    suddenFatigue = suddenFatigue,
                    brainFog = brainFog,
                    hardToStand = hardToStand,
                    notRecoveringSitting = notRecoveringSitting,
                    notEnoughEnergyToGoHome = notEnoughEnergyToGoHome,
                    keptScheduledRest = keptScheduledRest,
                    leftEarly = leftEarly,
                    earlyLeaveReason = earlyLeaveReason,
                    returnedSafely = returnedSafely,
                    selfEvaluation = SocialSelfEvaluationChoices[selfEvaluation],
                    nextDayWorse = NextDayWorseChoices[nextDayWorse]
                )
            ) {
                "レベル5の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (savedMessage.isNotBlank()) {
        MessageCard(text = savedMessage)
    }
    CalmNextStageButton(
        text = "次のレベルへ進む",
        onClick = onNextLevel6
    )
    CalmBackButton(onClick = onBack)
}

@Composable
private fun SocialRehabilitationLevel6Screen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var restedEveryTwoHours by remember { mutableStateOf(false) }
    var avoidedOvertime by remember { mutableStateOf(false) }
    var keptEnergyToGoHome by remember { mutableStateOf(false) }
    var avoidedShoppingAfterWork by remember { mutableStateOf(false) }
    var movedShoppingToDayOff by remember { mutableStateOf(false) }
    var consideredDeliveryOrSupport by remember { mutableStateOf(false) }
    var selfEvaluation by remember { mutableStateOf(0) }
    var nextDayWorse by remember { mutableStateOf(0) }
    var savedMessage by remember { mutableStateOf("") }

    ScreenTitle("社会復帰編 レベル6")
    Text(
        text = "社会復帰後の注意事項",
        color = TextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    SectionCard(
        title = "目的",
        body = "社会復帰できた状態を保つことが目的です。\n\n仕事に戻ることがゴールではありません。\n\n仕事を続けても翌日に大きく崩れない状態を作ることが、この段階の目的です。"
    )
    SectionCard(
        title = "1. 2時間に1回は休みを取る",
        body = "勤務中は、少なくとも2時間に1回は休息を取ってください。\n\n症状が強くなってから休むのではなく、\n\n症状が強くなる前に予定として休みを入れてください。"
    )
    SectionCard(
        title = "2. 超過勤務はしない",
        body = "社会復帰直後は、超過勤務を避けてください。\n\n「今日は大丈夫」と感じても、\n\n翌日に悪化することがあります。"
    )
    SectionCard(
        title = "3. 帰りの体力を残す",
        body = "仕事中に全ての体力を使い切らないでください。\n\n帰宅する体力を残すことが大切です。\n\n安全に帰宅できることも、社会復帰の一部です。"
    )
    SectionCard(
        title = "4. 仕事帰りに無理な買い物をしない",
        body = "仕事帰りの買い物は、負荷が大きくなりやすい活動です。\n\n仕事で疲れた後に買い物を追加すると、\n\n翌日に悪化することがあります。"
    )
    SectionCard(
        title = "5. 買い物は休みの日にまとめる",
        body = "可能であれば、買い物は休みの日に行ってください。\n\n1週間分をまとめて買う、\n\nまたは宅配を利用することも選択肢になります。\n\n宅配は費用がかかりますが、\n\n身体への負荷を減らす方法として検討できます。\n\nこれは簡単な選択ではありません。\n\nその日の体力、家庭の事情、費用を考えながら、\n\n無理の少ない方法を選んでください。"
    )
    Label("記録項目")
    CheckItem("2時間に1回休みを取れた", restedEveryTwoHours) { restedEveryTwoHours = it }
    CheckItem("超過勤務をしなかった", avoidedOvertime) { avoidedOvertime = it }
    CheckItem("帰宅する体力を残せた", keptEnergyToGoHome) { keptEnergyToGoHome = it }
    CheckItem("仕事帰りに無理な買い物をしなかった", avoidedShoppingAfterWork) { avoidedShoppingAfterWork = it }
    CheckItem("買い物を休みの日に回せた", movedShoppingToDayOff) { movedShoppingToDayOff = it }
    CheckItem("宅配や家族の協力を検討できた", consideredDeliveryOrSupport) { consideredDeliveryOrSupport = it }
    ChoiceField("実施後の自己評価", SocialSelfEvaluationChoices, selfEvaluation) { selfEvaluation = it }
    ChoiceField("翌日の評価", NextDayWorseChoices, nextDayWorse) { nextDayWorse = it }
    SectionCard(
        title = "次の段階へ進む目安",
        body = SocialRehabilitationLevel6NextCriteria
    )
    SectionCard(
        title = "一つ前の段階へ戻る目安",
        body = SocialRehabilitationLevel6BackCriteria
    )
    RecordButton(
        onClick = {
            savedMessage = if (
                saveSocialRehabilitationLevel6RecordSafely(
                    context = context,
                    restedEveryTwoHours = restedEveryTwoHours,
                    avoidedOvertime = avoidedOvertime,
                    keptEnergyToGoHome = keptEnergyToGoHome,
                    avoidedShoppingAfterWork = avoidedShoppingAfterWork,
                    movedShoppingToDayOff = movedShoppingToDayOff,
                    consideredDeliveryOrSupport = consideredDeliveryOrSupport,
                    selfEvaluation = SocialSelfEvaluationChoices[selfEvaluation],
                    nextDayWorse = NextDayWorseChoices[nextDayWorse]
                )
            ) {
                "レベル6の記録を保存しました。"
            } else {
                "記録できませんでした。もう一度お試しください。"
            }
        }
    )
    if (savedMessage.isNotBlank()) {
        MessageCard(text = savedMessage)
    }
    PainCompassGuidanceCard()
    CalmBackButton(onClick = onBack)
}

@Composable
private fun ExerciseRecordScreen(
    program: ExerciseProgram?,
    hasNext: Boolean,
    nextButtonText: String,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val currentProgram = program ?: DeepBreathingLevel1Program
    val now = remember(currentProgram) { Date() }
    val dateText = remember(currentProgram) { SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now) }
    val timeText = remember(currentProgram) { SimpleDateFormat("HH:mm", Locale.JAPAN).format(now) }
    var count by remember(currentProgram) { mutableStateOf(0) }
    var selfEvaluation by remember(currentProgram) { mutableStateOf(0) }
    var dizziness by remember(currentProgram) { mutableStateOf(0) }
    var breathlessness by remember(currentProgram) { mutableStateOf(0) }
    var strongPain by remember(currentProgram) { mutableStateOf(0) }
    var fallRisk by remember(currentProgram) { mutableStateOf(0) }
    var saved by remember(currentProgram) { mutableStateOf(false) }

    ScreenTitle("運動記録")
    Label("実施日")
    ValueBox(dateText)
    Label("実施時刻")
    ValueBox(timeText)
    SectionCard(
        title = "プログラム",
        body = "プログラム名：${currentProgram.programName}\n\nレベル：${currentProgram.level}\n\n実施内容：${currentProgram.content}"
    )
    ChoiceField("実施回数", currentProgram.countChoices, count) { count = it }
    ChoiceField("実施後の自己評価", AfterPracticeChoices, selfEvaluation) { selfEvaluation = it }
    Label("安全確認")
    ChoiceField("めまいなし", RecordSafetyChoices, dizziness) { dizziness = it }
    ChoiceField("息苦しさなし", RecordSafetyChoices, breathlessness) { breathlessness = it }
    ChoiceField("強い痛みなし", RecordSafetyChoices, strongPain) { strongPain = it }
    ChoiceField("転倒しそうな感じなし", RecordSafetyChoices, fallRisk) { fallRisk = it }

    if (saved) {
        MessageCard(text = "今日の記録を保存しました。")
    } else {
        PrimaryButton(
            text = "記録を保存",
            onClick = {
                saveExerciseRecord(
                    context = context,
                    dateText = dateText,
                    timeText = timeText,
                    program = currentProgram,
                    count = currentProgram.countChoices[count],
                    selfEvaluation = AfterPracticeChoices[selfEvaluation],
                    dizziness = RecordSafetyChoices[dizziness],
                    breathlessness = RecordSafetyChoices[breathlessness],
                    strongPain = RecordSafetyChoices[strongPain],
                    fallRisk = RecordSafetyChoices[fallRisk]
                )
                saved = true
            }
        )
    }

    if (saved && hasNext) {
        PrimaryButton(text = nextButtonText.ifBlank { "次へ" }, onClick = onContinue)
    }
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
                append("\n\nで現在位置を確認してください。\n\n現在位置を確認してから、\n無理のない範囲で生活・社会復帰を続けましょう。")
            },
            modifier = Modifier.padding(16.dp),
            color = TextPrimary,
            fontSize = 18.sp,
            lineHeight = 27.sp
        )
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
    program: ExerciseProgram,
    count: String,
    selfEvaluation: String,
    dizziness: String,
    breathlessness: String,
    strongPain: String,
    fallRisk: String,
    preExerciseMemo: String = ""
): Boolean {
    return runCatching {
        val now = Date()
        saveExerciseRecord(
            context = context,
            dateText = SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now),
            timeText = SimpleDateFormat("HH:mm", Locale.JAPAN).format(now),
            program = program,
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
    val record = JSONObject()
        .put("date", dateText)
        .put("time", timeText)
        .put("programName", program.programName)
        .put("level", program.level)
        .put("content", program.content)
        .put("count", count)
        .put("selfEvaluation", selfEvaluation)
        .put("preExerciseMemo", preExerciseMemo)
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

private fun saveSimpleSocialRehabilitationRecordSafely(
    context: Context,
    dateText: String,
    timeText: String,
    level: String,
    content: String,
    count: String,
    selfEvaluation: String,
    nextDayWorse: String
): Boolean {
    return runCatching {
        val preferences = context.getSharedPreferences(ExerciseRecordPreferencesName, Context.MODE_PRIVATE)
        val records = JSONArray(preferences.getString(ExerciseRecordListKey, "[]") ?: "[]")
        val record = JSONObject()
            .put("date", dateText)
            .put("time", timeText)
            .put("programName", "社会復帰プログラム")
            .put("level", level)
            .put("content", content)
            .put("count", count)
            .put("selfEvaluation", selfEvaluation)
            .put("nextDayWorse", nextDayWorse)
            .put("nextCriteria", "")
            .put("backCriteria", "")
            .put("preExerciseMemo", "")
            .put(
                "safety",
                JSONObject()
                    .put("dizziness", RecordNotChecked)
                    .put("breathlessness", RecordNotChecked)
                    .put("strongPain", RecordNotChecked)
                    .put("fallRisk", RecordNotChecked)
            )
        records.put(record)
        preferences.edit().putString(ExerciseRecordListKey, records.toString()).apply()
    }.isSuccess
}

private fun saveSocialRehabilitationLevel3RecordSafely(
    context: Context,
    level: String,
    content: String,
    checkedCompass: Boolean,
    sleptEnough: Boolean,
    avoidedOverwork: Boolean,
    commuteLine: String,
    returnedAsPlanned: Boolean,
    noStrongFatigue: Boolean,
    noStrongPain: Boolean,
    tookBreaks: Boolean,
    selfEvaluation: String,
    nextDayWorse: String,
    nextCriteria: String,
    backCriteria: String
): Boolean {
    return runCatching {
        val now = Date()
        val preferences = context.getSharedPreferences(ExerciseRecordPreferencesName, Context.MODE_PRIVATE)
        val records = JSONArray(preferences.getString(ExerciseRecordListKey, "[]") ?: "[]")
        val record = JSONObject()
            .put("date", SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now))
            .put("time", SimpleDateFormat("HH:mm", Locale.JAPAN).format(now))
            .put("programName", "社会復帰プログラム")
            .put("level", level)
            .put("content", content)
            .put(
                "count",
                recordCheckLine("疼痛コンパスで現在の状態を確認する", checkedCompass) + "\n\n" +
                    recordCheckLine("睡眠を十分にとる", sleptEnough) + "\n\n" +
                    recordCheckLine("無理な活動を控える", avoidedOverwork) + "\n\n" +
                    commuteLine + "\n\n" +
                    recordCheckLine("予定どおり帰宅できた", returnedAsPlanned) + "\n\n" +
                    recordCheckLine("強い疲労はなかった", noStrongFatigue) + "\n\n" +
                    recordCheckLine("強い痛みはなかった", noStrongPain) + "\n\n" +
                    recordCheckLine("途中で休憩をとれた", tookBreaks)
            )
            .put("selfEvaluation", selfEvaluation)
            .put("nextDayWorse", nextDayWorse)
            .put("nextCriteria", nextCriteria)
            .put("backCriteria", backCriteria)
            .put("preExerciseMemo", "")
            .put(
                "safety",
                JSONObject()
                    .put("dizziness", RecordNotChecked)
                    .put("breathlessness", RecordNotChecked)
                    .put("strongPain", RecordNotChecked)
                    .put("fallRisk", RecordNotChecked)
            )
        records.put(record)
        preferences.edit().putString(ExerciseRecordListKey, records.toString()).apply()
    }.isSuccess
}

private fun saveSocialRehabilitationLevel4RecordSafely(
    context: Context,
    selfEvaluation: String,
    nextDayWorse: String
): Boolean {
    return runCatching {
        val now = Date()
        val preferences = context.getSharedPreferences(ExerciseRecordPreferencesName, Context.MODE_PRIVATE)
        val records = JSONArray(preferences.getString(ExerciseRecordListKey, "[]") ?: "[]")
        val record = JSONObject()
            .put("date", SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now))
            .put("time", SimpleDateFormat("HH:mm", Locale.JAPAN).format(now))
            .put("programName", "社会復帰プログラム")
            .put("level", "レベル4")
            .put("content", SocialRehabilitationLevel4Content)
            .put("count", "社会復帰編 レベル4の終了時記録")
            .put("selfEvaluation", selfEvaluation)
            .put("nextDayWorse", nextDayWorse)
            .put("nextCriteria", SocialRehabilitationLevel4NextCriteria)
            .put("backCriteria", SocialRehabilitationLevel4BackCriteria)
            .put("preExerciseMemo", "")
            .put(
                "safety",
                JSONObject()
                    .put("dizziness", RecordNotChecked)
                    .put("breathlessness", RecordNotChecked)
                    .put("strongPain", RecordNotChecked)
                    .put("fallRisk", RecordNotChecked)
            )
        records.put(record)
        preferences.edit().putString(ExerciseRecordListKey, records.toString()).apply()
    }.isSuccess
}

private fun saveSocialRehabilitationLevel5RecordSafely(
    context: Context,
    dateText: String,
    timeText: String,
    workTime: String,
    restedEveryHour: Boolean,
    restedPosture: Boolean,
    recoveredAfterRest: Boolean,
    suddenPain: Boolean,
    suddenFatigue: Boolean,
    brainFog: Boolean,
    hardToStand: Boolean,
    notRecoveringSitting: Boolean,
    notEnoughEnergyToGoHome: Boolean,
    keptScheduledRest: Boolean,
    leftEarly: Boolean,
    earlyLeaveReason: String,
    returnedSafely: Boolean,
    selfEvaluation: String,
    nextDayWorse: String
): Boolean {
    return runCatching {
        val preferences = context.getSharedPreferences(ExerciseRecordPreferencesName, Context.MODE_PRIVATE)
        val records = JSONArray(preferences.getString(ExerciseRecordListKey, "[]") ?: "[]")
        val record = JSONObject()
            .put("date", dateText)
            .put("time", timeText)
            .put("programName", "社会復帰プログラム")
            .put("level", "レベル5")
            .put("content", "短時間勤務予行")
            .put(
                "count",
                "勤務時間：$workTime\n\n" +
                    recordCheckLine("1時間ごとに5分休めた", restedEveryHour) + "\n\n" +
                    recordCheckLine("休息中に座る、横になる、目を閉じるなどができた", restedPosture) + "\n\n" +
                    recordCheckLine("休息後に少し回復した", recoveredAfterRest) + "\n\n" +
                    recordCheckLine("痛みが急に強くなった", suddenPain) + "\n\n" +
                    recordCheckLine("疲労が急に強くなった", suddenFatigue) + "\n\n" +
                    recordCheckLine("頭が働かなくなった", brainFog) + "\n\n" +
                    recordCheckLine("立っていることがつらくなった", hardToStand) + "\n\n" +
                    recordCheckLine("座っていても回復しない", notRecoveringSitting) + "\n\n" +
                    recordCheckLine("帰宅する体力が残っていないと感じた", notEnoughEnergyToGoHome) + "\n\n" +
                    recordCheckLine("1時間ごとの5分休息を守れたか", keptScheduledRest) + "\n\n" +
                    recordCheckLine("早退したか", leftEarly) + "\n\n" +
                    "早退した理由：${earlyLeaveReason.ifBlank { "記録なし" }}\n\n" +
                    recordCheckLine("安全に帰宅できたか", returnedSafely)
            )
            .put("selfEvaluation", selfEvaluation)
            .put("nextDayWorse", nextDayWorse)
            .put("nextCriteria", SocialRehabilitationLevel5NextCriteria)
            .put("backCriteria", SocialRehabilitationLevel5BackCriteria)
            .put("preExerciseMemo", "")
            .put(
                "safety",
                JSONObject()
                    .put("dizziness", RecordNotChecked)
                    .put("breathlessness", RecordNotChecked)
                    .put("strongPain", RecordNotChecked)
                    .put("fallRisk", RecordNotChecked)
            )
        records.put(record)
        preferences.edit().putString(ExerciseRecordListKey, records.toString()).apply()
    }.isSuccess
}

private fun saveSocialRehabilitationLevel6RecordSafely(
    context: Context,
    restedEveryTwoHours: Boolean,
    avoidedOvertime: Boolean,
    keptEnergyToGoHome: Boolean,
    avoidedShoppingAfterWork: Boolean,
    movedShoppingToDayOff: Boolean,
    consideredDeliveryOrSupport: Boolean,
    selfEvaluation: String,
    nextDayWorse: String
): Boolean {
    return runCatching {
        val now = Date()
        val preferences = context.getSharedPreferences(ExerciseRecordPreferencesName, Context.MODE_PRIVATE)
        val records = JSONArray(preferences.getString(ExerciseRecordListKey, "[]") ?: "[]")
        val record = JSONObject()
            .put("date", SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).format(now))
            .put("time", SimpleDateFormat("HH:mm", Locale.JAPAN).format(now))
            .put("programName", "社会復帰プログラム")
            .put("level", "レベル6")
            .put("content", "社会復帰後の注意事項")
            .put(
                "count",
                recordCheckLine("2時間に1回休みを取れた", restedEveryTwoHours) + "\n\n" +
                    recordCheckLine("超過勤務をしなかった", avoidedOvertime) + "\n\n" +
                    recordCheckLine("帰宅する体力を残せた", keptEnergyToGoHome) + "\n\n" +
                    recordCheckLine("仕事帰りに無理な買い物をしなかった", avoidedShoppingAfterWork) + "\n\n" +
                    recordCheckLine("買い物を休みの日に回せた", movedShoppingToDayOff) + "\n\n" +
                    recordCheckLine("宅配や家族の協力を検討できた", consideredDeliveryOrSupport)
            )
            .put("selfEvaluation", selfEvaluation)
            .put("nextDayWorse", nextDayWorse)
            .put("nextCriteria", SocialRehabilitationLevel6NextCriteria)
            .put("backCriteria", SocialRehabilitationLevel6BackCriteria)
            .put("preExerciseMemo", "")
            .put(
                "safety",
                JSONObject()
                    .put("dizziness", RecordNotChecked)
                    .put("breathlessness", RecordNotChecked)
                    .put("strongPain", RecordNotChecked)
                    .put("fallRisk", RecordNotChecked)
            )
        records.put(record)
        preferences.edit().putString(ExerciseRecordListKey, records.toString()).apply()
    }.isSuccess
}

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

private fun mandalaLevelNumber(level: String): Int? {
    return Regex("""\d+""").find(level)?.value?.toIntOrNull()
}

private fun mandalaEvaluationNumber(selfEvaluation: String): Int? {
    return when {
        selfEvaluation.startsWith("①") -> 1
        selfEvaluation.startsWith("②") -> 2
        selfEvaluation.startsWith("③") -> 3
        selfEvaluation.startsWith("④") -> 4
        selfEvaluation.startsWith("⑤") -> 5
        selfEvaluation == "楽になった" -> 1
        selfEvaluation == "変わらない" -> 2
        selfEvaluation == "少しつらい" -> 3
        selfEvaluation == "思ったよりつらかった" -> 4
        selfEvaluation == "動けなくなった" -> 5
        else -> null
    }
}

private fun recoveryMandalaMarkers(
    records: List<SavedExerciseRecord>,
    levels: List<Int>,
    evaluations: List<Int>
): Map<Pair<Int, Int>, List<MandalaMarker>> {
    return records
        .mapNotNull { record ->
            val levelNumber = mandalaLevelNumber(record.level) ?: return@mapNotNull null
            val evaluationNumber = mandalaEvaluationNumber(record.selfEvaluation) ?: return@mapNotNull null
            if (levelNumber in levels && evaluationNumber in evaluations) {
                record to (levelNumber to evaluationNumber)
            } else {
                null
            }
        }
        .mapIndexed { index, recordCell ->
            val (record, cell) = recordCell
            val marker = when (index) {
                0 -> MandalaMarker("★", Color(0xFFFFC107), 19)
                1 -> MandalaMarker("★", Color.Black, 19)
                2 -> MandalaMarker("☆", Color.White, 19, Color(0xFF7A7A7A))
                else -> MandalaMarker("●", mandalaPeriodColor(record.date), 15, isHistoryDot = true)
            }
            cell to marker
        }
        .groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        )
}

private fun mandalaPeriodColor(date: String): Color {
    val recordDate = runCatching {
        SimpleDateFormat("yyyy年M月d日", Locale.JAPAN).parse(date)
    }.getOrNull() ?: return MandalaTwelveWeeksColor
    val days = ((Date().time - recordDate.time).coerceAtLeast(0L)) / (24L * 60L * 60L * 1000L)
    return when {
        days <= 7L -> MandalaOneWeekColor
        days <= 14L -> MandalaTwoWeeksColor
        days <= 28L -> MandalaFourWeeksColor
        days <= 56L -> MandalaEightWeeksColor
        else -> MandalaTwelveWeeksColor
    }
}

private fun compactMandalaMarkers(markers: List<MandalaMarker>): List<MandalaMarker> {
    val stars = markers.filterNot { it.isHistoryDot }
    val dots = markers.filter { it.isHistoryDot }
    if (dots.size <= 3) return stars + dots

    val compactDot = MandalaMarker(
        symbol = "●×${dots.size}",
        color = dots.firstOrNull()?.color ?: MandalaTwelveWeeksColor,
        fontSize = 13,
        isHistoryDot = true
    )
    return stars + compactDot
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

private fun loadExerciseRecordsSafely(context: Context): List<SavedExerciseRecord> {
    return runCatching {
        val preferences = context.getSharedPreferences(ExerciseRecordPreferencesName, Context.MODE_PRIVATE)
        val records = JSONArray(preferences.getString(ExerciseRecordListKey, "[]") ?: "[]")
        List(records.length()) { index ->
            val record = records.getJSONObject(index)
            val safety = record.optJSONObject("safety")
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
                nextDayWorse = record.optString("nextDayWorse"),
                nextCriteria = record.optString("nextCriteria"),
                backCriteria = record.optString("backCriteria")
            )
        }.asReversed()
    }.getOrDefault(emptyList())
}

private val PainChoices = listOf("痛くない", "軽い", "少しある", "強い", "かなり強い", "動くのがつらい")
private val FatigueChoices = listOf("無し", "軽い", "少しある", "強い", "かなり強い", "休息が必要")
private val SleepChoices = listOf("眠れた", "少し眠れた", "眠りが浅い", "ほとんど眠れない")
private val BreathingChoices = listOf("落ち着いている", "少し浅い", "苦しさがある", "苦しさが強い")
private val ComparisonChoices = listOf("昨日より良い", "少し良い", "変わらない", "少し悪い", "かなり悪い")
private val AbilityChoices = listOf("できる", "少しならできる", "今日は難しい")
private val SafetyCheckChoices = listOf("ない", "少しある", "ある")
private val AfterPracticeChoices = listOf("① 楽になった", "② 変わらない", "③ 少しつらい", "④ 思ったよりつらかった", "⑤ 動けなくなった")
private val SocialSelfEvaluationChoices = listOf("① 楽になった", "② 変わらない", "③ 少しつらい", "④ 思ったよりつらかった", "⑤ 動けなくなった")
private val RecordSafetyChoices = listOf("はい", "少し不安あり", "いいえ")
private val HoldTenSecondsChoices = listOf("できた", "少しできた", "今日は難しい")
private val SocialTimeOfDayChoices = listOf("午前", "午後")
private val SocialWalkingMinuteChoices = listOf("1分", "2分", "3分", "4分", "5分")
private val NextDayWorseChoices = listOf("① 悪化なし", "② 少し悪化", "③ 明らかな悪化")
private val ShortWorkTimeChoices = listOf("1時間以内", "2時間以内", "3時間以内", "4時間以内")
private val SocialRehabilitationDetailedRecordLevels = setOf("レベル3-A", "レベル3-B", "レベル4", "レベル5", "レベル6")
private const val SocialRehabilitationLevel3TransitNextCriteria = "□ 公共交通機関を安全に利用できた\n\n□ 予定どおり帰宅できた\n\n□ 翌日に大きな悪化がなかった\n\n□ 活動後も回復できた\n\nすべてを確認できたら、次の段階へ進むことを考えましょう。"
private const val SocialRehabilitationLevel3TransitBackCriteria = "□ 翌日に明らかな悪化があった\n\n□ 帰宅後に寝込んだ\n\n□ 安全に帰宅できなかった\n\n□ 通勤による負荷が大きすぎた\n\n一つでも当てはまる場合は、一つ前の段階へ戻ることを考えましょう。"
private const val SocialRehabilitationLevel3CarNextCriteria = "□ 自家用車通勤の流れを安全に確認できた\n\n□ 予定どおり帰宅できた\n\n□ 翌日に大きな悪化がなかった\n\n□ 活動後も回復できた\n\nすべてを確認できたら、次の段階へ進むことを考えましょう。"
private const val SocialRehabilitationLevel3CarBackCriteria = "□ 翌日に明らかな悪化があった\n\n□ 帰宅後に寝込んだ\n\n□ 安全に帰宅できなかった\n\n□ 運転を含む通勤による負荷が大きすぎた\n\n一つでも当てはまる場合は、一つ前の段階へ戻ることを考えましょう。"
private const val SocialRehabilitationLevel4Content = "職場滞在予行：職場に入り、短時間滞在して帰ります。"
private const val SocialRehabilitationLevel4NextCriteria = "□ 翌日に大きな悪化がなかった\n\n□ 活動後も回復できた\n\n□ 安全に終了できた"
private const val SocialRehabilitationLevel4BackCriteria = "□ 翌日に明らかな悪化があった\n\n□ 活動後に寝込んだ\n\n□ 負荷が大きすぎた"
private const val SocialRehabilitationLevel5NextCriteria = "□ 1時間ごとの5分休息を守れた\n\n□ 勤務中に大きく崩れなかった\n\n□ 安全に帰宅できた\n\n□ 翌日に大きな悪化がなかった\n\nすべてを確認できたら、次の段階へ進むことを考えましょう。"
private const val SocialRehabilitationLevel5BackCriteria = "□ 休息を入れても回復しなかった\n\n□ 早退が必要だった\n\n□ 帰宅後に寝込んだ\n\n□ 翌日に明らかな悪化があった\n\n一つでも当てはまる場合は、一つ前の段階へ戻ることを考えましょう。"
private const val SocialRehabilitationLevel6NextCriteria = "□ 2時間に1回の休息を守れた\n\n□ 超過勤務をしなかった\n\n□ 安全に帰宅できた\n\n□ 翌日に大きな悪化がなかった\n\nすべてを確認できたら、社会復帰を継続することを考えましょう。"
private const val SocialRehabilitationLevel6BackCriteria = "□ 超過勤務をしてしまった\n\n□ 帰宅後に寝込んだ\n\n□ 仕事帰りの買い物で悪化した\n\n□ 翌日に明らかな悪化があった\n\n一つでも当てはまる場合は、一つ前の段階へ戻ることを考えましょう。"
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

private val ScreenBackground = Color(0xFFF7FAF8)
private val TextPrimary = Color.Black
private val TextHint = Color(0xFF555555)
private val PastelGreenButton = Color(0xFF4CAF72)
private val MandalaPurpleButton = Color(0xFF5E548E)
private val CalmDeepTeal = Color(0xFF1F6B5C)
private val CalmSubButtonBorder = Color(0xFFE0E3E1)
private val MandalaOneWeekColor = Color.Black
private val MandalaTwoWeeksColor = Color(0xFFC62828)
private val MandalaFourWeeksColor = Color(0xFF7B1FA2)
private val MandalaEightWeeksColor = Color(0xFF2E7D32)
private val MandalaTwelveWeeksColor = Color(0xFF1565C0)

@Preview(showBackground = true)
@Composable
private fun FibromyalgiaFirstStepPreview() {
    FMEXFirstStepTheme {
        FibromyalgiaFirstStepApp()
    }
}
