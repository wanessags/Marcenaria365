
package com.nexo.marcenaria365.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF193447)
private val Cobre = Color(0xFFBD8B60)
private val BegeIcone = Color(0xFFF5E8D9)

@Composable
fun PrivacyScreen(
    onAgreeClick: () -> Unit = {}
) {
    var concordou by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp)
            .padding(top = 24.dp, bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .background(BegeIcone, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = Cobre
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Seus dados sempre protegidos",
            fontSize = 21.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Azul,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))

        Text(
            text = "A Marcenaria365 foi pensada para ajudar " +
                    "você a organizar seu trabalho com " +
                    "tranquilidade. Seus dados serão tratados " +
                    "conforme nossa Política de Privacidade.",
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = Azul,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        ItemPrivacidade(
            Icons.Outlined.Lock,
            "Proteção das suas informações"
        )

        ItemPrivacidade(
            Icons.Outlined.Cloud,
            "Armazenamento e backup conforme os serviços disponíveis"
        )

        ItemPrivacidade(
            Icons.Outlined.VerifiedUser,
            "Controle sobre seus dados pessoais"
        )

        ItemPrivacidade(
            Icons.Outlined.Description,
            "Compromisso com a privacidade e a LGPD"
        )

        Spacer(Modifier.height(36.dp))

        Text(
            text = "Antes de continuar, leia nossos",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Termos de Uso e Política de Privacidade",
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.SemiBold,
            color = Azul,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    concordou = !concordou
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = concordou,
                onCheckedChange = { concordou = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Azul,
                    uncheckedColor = Cobre,
                    checkmarkColor = Color.White
                )
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = "Li e concordo com os Termos de Uso e a Política de Privacidade.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Azul,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = onAgreeClick,
            enabled = concordou,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(11.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Azul,
                contentColor = Color.White,
                disabledContainerColor = Azul.copy(alpha = 0.40f),
                disabledContentColor = Color.White
            )
        ) {
            Text(
                text = "Continuar",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ItemPrivacidade(
    icone: ImageVector,
    descricao: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(BegeIcone, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = Cobre,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = descricao,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = Azul,
            modifier = Modifier.weight(1f)
        )
    }
}
