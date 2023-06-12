package com.metinkale.prayer.times.calc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.compose.theme.AppTheme
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.times.times.Vakit
import dev.metinkale.prayertimes.calc.HighLatsAdjustment
import dev.metinkale.prayertimes.calc.Method
import kotlinx.datetime.toKotlinLocalDate
import java.text.DecimalFormat


@ExperimentalMaterial3Api
@Composable
fun PrayTimesConfigurationView(model: PrayTimesConfigurationViewModel) = AppTheme {
    val praytimes = model.prayTimes.collectAsState()
    val asrType = model.asrType.collectAsState()
    val method: Method = praytimes.value.method
    val daytimes = praytimes.value.getTimes(java.time.LocalDate.now().toKotlinLocalDate())
    val highLats: HighLatsAdjustment = praytimes.value.method.highLats

    val angleTranslations = HighLatsAdjustment.values().associateWith {
        stringResource(
            when (it) {
                HighLatsAdjustment.None -> R.string.noAdjustment
                HighLatsAdjustment.AngleBased -> R.string.adjAngle
                HighLatsAdjustment.OneSeventh -> R.string.adjTime
                HighLatsAdjustment.NightMiddle -> R.string.adjMidnight
            }
        )
    }
    Column(
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            Modifier
                .padding(all = 8.dp)
                .fillMaxWidth()
        ) {
            SelectMenu(
                label = "Method",
                value = method,
                items = Method.values().mapNotNull { it as? Method },
                itemLabel = { it.name },
                itemSubLabel = { it.location },
                onChange = { model.setMethod(it) }
            )
            SelectMenu(
                label = "Anpassung für hohe Breitengrade",
                value = highLats,
                items = HighLatsAdjustment.values().toList(),
                itemLabel = { angleTranslations[it] ?: "" },
                onChange = { model.setHighLats(it) },
                modifier = Modifier.padding(top = 16.dp)
            )
        }


        val rows = Vakit.values()
            .flatMap { if (it == Vakit.ASR) listOf(it to 0, it to 1) else listOf(it to 0) }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column {
                Row(modifier = Modifier.height(32.dp)) {
                    Text(
                        "Vakit",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                rows.forEach { (vakit, num) ->
                    Row(modifier = Modifier.height(32.dp)) {
                        if (vakit == Vakit.ASR) {
                            Box(Modifier.size(24.dp)) {
                                if (num == 0)
                                    Checkbox(
                                        asrType.value != Times.AsrType.Hanafi,
                                        { model.setAsrType(Times.AsrType.Shafi, it) })
                                else
                                    Checkbox(asrType.value != Times.AsrType.Shafi,
                                        { model.setAsrType(Times.AsrType.Hanafi, it) })
                            }
                        }
                        Text(
                            modifier = Modifier.padding(start = if (vakit == Vakit.ASR) 4.dp else 0.dp),
                            text = runCatching { vakit.getString(num) }.getOrNull()
                                ?: "Time",
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            Column {
                Row(modifier = Modifier.height(32.dp)) {
                    Text(
                        "Saat", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                rows.forEach { (vakit, num) ->
                    Row(modifier = Modifier.height(32.dp)) {
                        Text(
                            text = daytimes.run {
                                when (vakit) {
                                    Vakit.FAJR -> fajr
                                    Vakit.SUN -> sunrise
                                    Vakit.DHUHR -> dhuhr
                                    Vakit.ASR -> if (num == 0) asrShafi else asrHanafi
                                    Vakit.MAGHRIB -> maghrib
                                    Vakit.ISHAA -> ishaa
                                }
                            }.toString(),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Column(modifier = Modifier.width(90.dp)) {
                Row(modifier = Modifier.height(32.dp)) {
                    Text(
                        "Aci", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                rows.forEach { (vakit, _) ->
                    Box(modifier = Modifier.height(32.dp)) {
                        if (vakit in listOf(Vakit.FAJR, Vakit.MAGHRIB, Vakit.ISHAA)) {
                            val angle = model.getAngle(vakit)!!
                            val set: (Double) -> Unit = { model.setAngleDrift(vakit, it) }
                            val setStr: (String) -> Unit = { set(it.toDoubleOrNull() ?: angle) }
                            val incr: () -> Unit = { set(angle + 0.1) }
                            val decr: () -> Unit = { set(angle - 0.1) }
                            BasicTextField(
                                value = ONE_DECIMALS.format(angle),
                                onValueChange = setStr,
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier.align(Alignment.Center)
                            )

                            Icon(
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(4.dp)
                                    .clickable(onClick = decr)
                                    .align(Alignment.CenterStart),
                                imageVector = Icons.Default.Remove,
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = "Remove",
                            )
                            Icon(
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(4.dp)
                                    .clickable(onClick = incr)
                                    .align(Alignment.CenterEnd),
                                imageVector = Icons.Default.Add,
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = "Add",
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.width(90.dp)) {
                Row(modifier = Modifier.height(32.dp)) {
                    Text(
                        "Zaman", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                rows.forEach { (vakit, num) ->
                    val min = model.getMinuteDrift(vakit, num)
                    val set: (Int) -> Unit = { model.setMinuteDrift(vakit, num, it) }
                    val setStr: (String) -> Unit = { set(it.toIntOrNull() ?: min) }
                    val incr: () -> Unit = { set(min + 1) }
                    val decr: () -> Unit = { set(min - 1) }
                    Box(modifier = Modifier.height(32.dp)) {
                        BasicTextField(
                            value = min.toString(),
                            onValueChange = setStr,
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.align(Alignment.Center)
                        )

                        Icon(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                                .align(Alignment.CenterEnd)
                                .clickable(onClick = incr),
                            imageVector = Icons.Default.Add,
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = "Add"
                        )
                        Icon(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                                .align(Alignment.CenterStart)
                                .clickable(onClick = decr),
                            imageVector = Icons.Default.Remove,
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = "Remove"
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.End
        ) {
            Button(onClick = { model.save() }) {
                Text(text = "Speichern")
            }
        }

    }
}

private val Method.location
    get() = when (this) {
        Method.MWL -> "Europe, Far East, parts of US"
        Method.ISNA -> "North America (US and Canada)"
        Method.Egypt -> "Africa, Syria, Lebanon, Malaysia"
        Method.Makkah -> "Arabian Peninsula"
        Method.Karachi -> "Pakistan, Afganistan, Bangladesh, India"
        Method.UOIF -> "France"
        Method.Tehran -> "Iran, Some Shia communities"
        Method.Jafari -> "Some Shia communities worldwide"
        else -> ""
    }


private val ONE_DECIMALS = DecimalFormat("#.#")
