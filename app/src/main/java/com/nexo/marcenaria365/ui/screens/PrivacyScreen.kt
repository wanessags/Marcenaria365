
package com.nexo.marcenaria365.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Fundo = Color(0xFFFAF6F0)
private val Azul = Color(0xFF1A3445)
private val Cobre = Color(0xFFBD8B60)
private val BegeIcone = Color(0xFFF5E8D9)

@Composable
fun PrivacyScreen(
    onAgreeClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fundo)
            .padding(horizontal = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(44.dp))

            Box(
                modifier = Modifier
                    .size(94.dp)
                    .background(BegeIcone, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = Cobre
                )
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = "Seus dados sempre protegidos",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Azul,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "A Marcenaria365 foi pensada para você " +
                        "trabalhar com tranquilidade e segurança. " +
                        "Seus dados são armazenados com proteção " +
                        "e utilizados apenas para melhorar sua " +
                        "experiência e apoiar o seu negócio.",
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = Azul
            )

            Spacer(Modifier.height(22.dp))

            PrivacyItem(
                Icons.Outlined.Lock,
                "Seus dados estão protegidos"
            )
            PrivacyItem(
                Icons.Outlined.Cloud,
                "Backup automático e seguro na nuvem"
            )
            PrivacyItem(
                Icons.Outlined.VerifiedUser,
                "Você tem controle sobre seus dados"
            )
            PrivacyItem(
                Icons.Outlined.Description,
                "Segurança e conformidade com a LGPD"
            )
        }

        Text(
            text = "Ao continuar, você concorda com nossos",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "Termos de Uso e Política de Privacidade.",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Azul,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = onAgreeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Azul
            )
        ) {
            Text(
                text = "Concordar e continuar",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun PrivacyItem(
    icon: ImageVector,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(43.dp)
                .background(BegeIcone, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Cobre,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = description,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = Azul
        )
    }
}
