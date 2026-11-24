
package com.nexo.marcenaria365.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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

    var documentoAberto by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    // Os documentos são exibidos na própria etapa
    // de aceite, preservando o estado do formulário.
    if (documentoAberto != null) {
        val tipoDocumento =
            if (documentoAberto == "termos") {
                TipoDocumentoLegal.TERMOS
            } else {
                TipoDocumentoLegal.PRIVACIDADE
            }

        DocumentosLegaisScreen(
            tipo = tipoDocumento,
            onVoltar = {
                documentoAberto = null
            }
        )
        return
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
                        color = Color(0xFFF3E5D8),
                        shape = RoundedCornerShape(18.dp)
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
                lineHeight = 30.sp,
                modifier = Modifier.semantics {
                    heading()
                }
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

            // CARTÃO ORIGINAL PRESERVADO

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sobre o aplicativo",
                        color = AzulPrivacidade,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "O Marcenaria 365 ajuda a organizar " +
                                "clientes, serviços, orçamentos " +
                                "e movimentações financeiras.",
                        color = CinzaPrivacidade,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )

                    Text(
                        text = "Esta versão faz parte de um projeto " +
                                "acadêmico e utiliza dados fictícios " +
                                "e funcionalidades em desenvolvimento.",
                        color = CinzaPrivacidade,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(17.dp))

            // POLÍTICA ORIGINAL + TERMOS DE USO

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column {
                    DocumentoOpcao(
                        titulo = "Política de Privacidade",
                        subtitulo = "Toque para ler o documento completo",
                        onClick = {
                            documentoAberto = "privacidade"
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            horizontal = 17.dp
                        ),
                        color = Color(0xFFEAE5DE)
                    )

                    DocumentoOpcao(
                        titulo = "Termos de Uso",
                        subtitulo = "Toque para ler as condições de utilização",
                        onClick = {
                            documentoAberto = "termos"
                        }
                    )
                }
            }

            Spacer(Modifier.height(25.dp))

            // ACEITE ORIGINAL PRESERVADO

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.Checkbox
                    ) {
                        aceitouTermos = !aceitouTermos
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = aceitouTermos,
                    onCheckedChange = { aceitou ->
                        aceitouTermos = aceitou
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AzulPrivacidade,
                        checkmarkColor = Color.White
                    )
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = "Li e concordo com os Termos de Uso " +
                            "e com a Política de Privacidade.",
                    color = AzulPrivacidade,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(25.dp))
        }

        // BOTÃO ORIGINAL FIXO
        // Continua desabilitado sem o aceite.

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
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFBEC7CA),
                disabledContentColor = Color.White
            )
        ) {
            Text(
                text = "Continuar",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(9.dp))

        // SOMENTE A VERSÃO ABAIXO DO BOTÃO.

        Text(
            text = "Versão 1.0",
            color = CinzaPrivacidade,
            fontSize = 11.sp,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DocumentoOpcao(
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .heightIn(min = 78.dp)
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
                text = titulo,
                color = AzulPrivacidade,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = subtitulo,
                color = CinzaPrivacidade,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = CinzaPrivacidade,
            modifier = Modifier.size(19.dp)
        )
    }
}
