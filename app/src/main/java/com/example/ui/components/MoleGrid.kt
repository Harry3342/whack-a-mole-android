package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.FloatingScore
import com.example.ui.MalletSwing
import com.example.ui.theme.MeadowBackgroundDark
import com.example.ui.theme.MeadowGreenDark
import com.example.ui.theme.SoilBrownDark

@Composable
fun MoleGrid(
    activeHoleIndex: Int?,
    whackedHoleIndex: Int?,
    floatingScores: List<FloatingScore>,
    activeMallet: MalletSwing?,
    onHoleTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 440.dp)
            .testTag("mole_grid_board"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val isHoleActive = (activeHoleIndex == index)
                        val isHoleWhacked = (whackedHoleIndex == index)
                        val floatItem = floatingScores.findLast { it.holeIndex == index }
                        val malletItem = if (activeMallet?.holeIndex == index) activeMallet else null

                        MoleHole(
                            index = index,
                            isActive = isHoleActive,
                            isWhacked = isHoleWhacked,
                            floatingScore = floatItem,
                            malletSwing = malletItem,
                            onTap = { onHoleTapped(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
