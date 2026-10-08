
package com.nexo.marcenaria365.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoPrivacidade = Color(0xFFFAF6F0)
private val AzulPrivacidade = Color(0xFF193447)
private val CobrePrivacidade = Color(0xFFBD906E)
private val CinzaPrivacidade = Color(0xFF929292)

@Composable
fun PrivacyScreen(
    onAgreeClick: () -> Unit,
    onReadPolicy: () -> Unit
) {
    var aceitouTermos by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoPrivacidade)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp)
        ) {
            Spacer(Modifier.height(38.dp))

            Box(
                modifier = Modifier
                    .size(66.dp)
                    .background(
                        Color(0xFFF3E5D8),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = CobrePrivacidade,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(25.dp))

            Text(
                text = "Seus dados, sua privacidade",
                color = AzulPrivacidade,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Antes de continuar, conheça as informações " +
                        "sobre privacidade e utilização do Marcenaria 365.",
                color = CinzaPrivacidade,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sobre o aplicativo",
                        color = AzulPrivacidade,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "O Marcenaria 365 foi desenvolvido " +
                                "para ajudar na organização de clientes, " +
                                "serviços, orçamentos e movimentações financeiras.",
                        color = CinzaPrivacidade,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )

                    Text(
                        text = "Esta versão faz parte de um projeto " +
                                "acadêmico e utiliza dados fictícios " +
                                "e recursos em desenvolvimento.",
                        color = CinzaPrivacidade,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(17.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onReadPolicy),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = CobrePrivacidade,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(13.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Política de Privacidade",
                            color = AzulPrivacidade,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = "Toque para ler o documento completo",
                            color = CinzaPrivacidade,
                            fontSize = 11.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Ler política",
                        tint = CinzaPrivacidade,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(Modifier.height(25.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        aceitouTermos = !aceitouTermos
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = aceitouTermos,
                    onCheckedChange = {
                        aceitouTermos = it
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AzulPrivacidade,
                        checkmarkColor = Color.White
                    )
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = "Li e concordo com os termos de uso " +
                            "e com a Política de Privacidade.",
                    color = AzulPrivacidade,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(25.dp))
        }

        Button(
            onClick = onAgreeClick,
            enabled = aceitouTermos,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp)
                .height(53.dp),
            shape = RoundedCornerShape(11.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulPrivacidade,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Continuar",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}
