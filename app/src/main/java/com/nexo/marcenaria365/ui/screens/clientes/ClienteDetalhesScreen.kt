
package com.nexo.marcenaria365.ui.screens.clientes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClienteDetalhesScreen(
    cliente: ClienteUi,
    onVoltar: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: (Long) -> Unit
) {
    var confirmarExclusao by remember {
        mutableStateOf(false)
    }

    BackHandler(onBack = onVoltar)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClienteCores.Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // Cabeçalho
        ClienteCabecalho(
            titulo = "Detalhes do cliente",
            subtitulo = "Informações de cadastro e contato.",
            onVoltar = onVoltar
        )

        Spacer(Modifier.height(26.dp))

        // Identificação do cliente
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ClienteAvatar(
                nome = cliente.nome,
                tamanho = 76.dp
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = cliente.nome,
                color = ClienteCores.Azul,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(9.dp))

            ClienteStatus(
                ativo = cliente.ativo
            )
        }

        Spacer(Modifier.height(28.dp))

        ClienteTituloSecao(
            titulo = "Informações de contato"
        )

        Spacer(Modifier.height(12.dp))

        // Cartão de informações
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                ClienteInformacao(
                    titulo = "Nome completo",
                    valor = cliente.nome
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 17.dp),
                    color = ClienteCores.Borda
                )

                ClienteInformacao(
                    titulo = "Telefone",
                    valor = cliente.telefone
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 17.dp),
                    color = ClienteCores.Borda
                )

                ClienteInformacao(
                    titulo = "E-mail",
                    valor = cliente.email
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        // AÇÃO PRINCIPAL: EDITAR CLIENTE
        Button(
            onClick = onEditar,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ClienteCores.Azul,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(9.dp))

            Text(
                text = "Editar cliente",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(12.dp))

        // AÇÃO SECUNDÁRIA: EXCLUIR CLIENTE
        // Sem borda, sem fundo e com destaque discreto
        TextButton(
            onClick = {
                confirmarExclusao = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = ClienteCores.Vermelho
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null,
                tint = ClienteCores.Vermelho,
                modifier = Modifier.size(17.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Excluir cliente",
                color = ClienteCores.Vermelho,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(28.dp))
    }

    // Confirmação de exclusão
    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = {
                confirmarExclusao = false
            },

            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = ClienteCores.Vermelho
                )
            },

            title = {
                Text(
                    text = "Excluir cliente?",
                    color = ClienteCores.Azul,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    text = "Deseja realmente excluir " +
                            "${cliente.nome}? Essa ação " +
                            "não poderá ser desfeita.",
                    color = ClienteCores.Cinza
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                        onExcluir(cliente.id)
                    }
                ) {
                    Text(
                        text = "Excluir",
                        color = ClienteCores.Vermelho,
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = ClienteCores.Azul
                    )
                }
            },

            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
