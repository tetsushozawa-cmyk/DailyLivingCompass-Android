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
import com.example.fmexfirststep.ui.theme.DailyLivingCompassTheme
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
    BasicInfo,
    SelfCheck,
    Result,
    BeforeStart,
    DeepBreathingLevel1,
    DeepBreathingLevel2,
    StandingLevel3,
    DeepSquatLevel32,
    IndoorWalkingLevel4,
    FamilyProgramCompleted
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
private fun DailyLivingCompassApp() {
    var step by remember { mutableStateOf(AppStep.Top) }
    var checkState by remember { mutableStateOf(SelfCheckState()) }
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }
    var basicMemo by remember { mutableStateOf("") }
    var level by remember { mutableStateOf<ProgramLevel?>(null) }
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
        title = "初期曼荼羅",
        subtitle = "レベル1〜2",
        levels = initialLevels,
        evaluations = evaluations,
        mandalaMarkers = initialMandalaMarkers
    )
    MessageCard(text = "詳細は疼痛コンパスを使用してください。")
    RecoveryMandalaGrid(
        title = "後半曼荼羅",
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
private fun DailyLivingCompassPreview() {
    DailyLivingCompassTheme {
        DailyLivingCompassApp()
    }
}
