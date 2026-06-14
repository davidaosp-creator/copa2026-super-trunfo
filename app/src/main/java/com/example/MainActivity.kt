package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

// Firebase Realtime Database
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

// Game Screens / Tabs
enum class GameTab {
    JOGAR, RANKING, PERFIL
}

// Game Modes
enum class PlayMode {
    NONE, MATCHMAKING, CPU_GAME, ONLINE_GAME
}

// Match Result
enum class MatchResult {
    WIN, LOSS, TIE
}

// Player Card Model
data class PlayerCard(
    val id: String,
    val name: String,
    val country: String,
    val position: String,
    val attack: Int,
    val defense: Int,
    val technique: Int,
    val heightInCm: Int,
    val speed: Int,
    val imageUrl: String,
    val isGold: Boolean = false,
    val legendBio: String = ""
)

// Primary Player database/deck
val PLAYERS_DECK = listOf(
    // LENDAS
    PlayerCard("pele", "PELÉ", "BRA", "ATA", 99, 45, 99, 173, 95,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Pel%C3%A9_1970.jpg/440px-Pel%C3%A9_1970.jpg",
        isGold = true, legendBio = "O Rei do Futebol, único tricampeão mundial da FIFA e considerado o maior atleta de todos os tempos."),

    PlayerCard("ronaldo", "RONALDO FENÔMENO", "BRA", "ATA", 98, 38, 97, 183, 97,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/2/22/Ronaldo_R9.jpg/440px-Ronaldo_R9.jpg",
        isGold = true, legendBio = "Dono de arranque e finalização devastadores, campeão mundial em 2002 e lenda implacável da Camisa 9."),

    PlayerCard("ronaldinho", "RONALDINHO GAÚCHO", "BRA", "MEI", 97, 35, 99, 181, 92,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e6/Ronaldinho_2.jpg/440px-Ronaldinho_2.jpg",
        isGold = true, legendBio = "O Bruxo do futebol, conhecido por dribles impossíveis, genialidade mágica e alegria contagiante em campo."),

    PlayerCard("zico", "ZICO", "BRA", "MEI", 96, 42, 98, 172, 86,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Zico_1981.jpg/440px-Zico_1981.jpg",
        isGold = true, legendBio = "O Galinho de Quintino, criador do jogo coletivo do Flamengo de 81 e cobra de faltas com precisão cirúrgica."),

    PlayerCard("maradona", "MARADONA", "ARG", "MEI", 98, 36, 99, 165, 88,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Maradona-Mundial_86_vs_Inglaterra_2.jpg/440px-Maradona-Mundial_86_vs_Inglaterra_2.jpg",
        isGold = true, legendBio = "Gênio folclórico, guiou a Argentina ao título da Copa de 86 com lances históricos e técnica extraordinária."),

    PlayerCard("cr7", "CRISTIANO RONALDO", "POR", "ATA", 98, 45, 95, 187, 94,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Cristiano_Ronaldo_2018.jpg/440px-Cristiano_Ronaldo_2018.jpg",
        isGold = true, legendBio = "Máquina indiscutível de gols, dono de 5 prêmios Ballon d'Or e sinônimo supremo de foco e dedicação física."),

    PlayerCard("messi", "LIONEL MESSI", "ARG", "ATA", 97, 38, 99, 170, 85,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/Lionel_Messi_20180626.jpg/440px-Lionel_Messi_20180626.jpg",
        isGold = true, legendBio = "Vencedor de 8 Bolas de Ouro, campeão mundial lendário de 2022 e dotado de uma visão de jogo inigualável."),

    PlayerCard("beckenbauer", "FRANZ BECKENBAUER", "GER", "DEF", 85, 98, 96, 181, 83,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f0/Franz_Beckenbauer.jpg/440px-Franz_Beckenbauer.jpg",
        isGold = true, legendBio = "Der Kaiser, criador do líbero clássico moderno, venceu a Copa como capitão (74) e como treinador (90)."),

    PlayerCard("garrincha", "GARRINCHA", "BRA", "ATA", 96, 30, 99, 169, 94,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/Garrincha.jpg/440px-Garrincha.jpg",
        isGold = true, legendBio = "A Alegria do Povo, bicampeão do mundo conhecido pelas fintas espetaculares que entortavam defensores de vez."),

    PlayerCard("distefano", "ALFREDO DI STÉFANO", "ARG", "ATA", 97, 50, 96, 178, 90,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Alfredo_di_Stefano.jpg/440px-Alfredo_di_Stefano.jpg",
        isGold = true, legendBio = "A Flecha Loira, comandante dinâmico do Real Madrid que faturou 5 Copas dos Campeões consecutivas."),

    // PADRÃO
    PlayerCard("mbappe", "KYLIAN MBAPPÉ", "FRA", "ATA", 96, 35, 92, 178, 97,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/2019129095833_2019-05-09_Fussball_Champions_League_FC_Bayern_vs_FC_Liverpool_-_Sven_-_1D_X_MK_II_-_0001_-_B70I0148.jpg/440px-thumbnail.jpg"),

    PlayerCard("vini", "VINI JR.", "BRA", "ATA", 95, 32, 94, 176, 95,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Vinicius_Junior_2022.jpg/440px-Vinicius_Junior_2022.jpg"),

    PlayerCard("bellingham", "JUDE BELLINGHAM", "ENG", "MEI", 91, 78, 90, 186, 79,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Jude_Bellingham_2021.jpg/440px-Jude_Bellingham_2021.jpg"),

    PlayerCard("de_bruyne", "KEVIN DE BRUYNE", "BEL", "MEI", 88, 64, 94, 181, 74,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2f/Kevin_De_Bruyne_2021.jpg/440px-Kevin_De_Bruyne_2021.jpg"),

    PlayerCard("haaland", "ERLING HAALAND", "NOR", "ATA", 95, 42, 83, 195, 89,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/5/51/Erling_Haaland_2023.jpg/440px-Erling_Haaland_2023.jpg"),

    PlayerCard("van_dijk", "VIRGIL VAN DIJK", "NED", "DEF", 60, 96, 85, 193, 78,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/Virgil_van_Dijk_2022.jpg/440px-Virgil_van_Dijk_2022.jpg"),

    PlayerCard("kane", "HARRY KANE", "ENG", "ATA", 93, 48, 87, 188, 67,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Harry_Kane_2022.jpg/440px-Harry_Kane_2022.jpg"),

    PlayerCard("modric", "LUKA MODRIĆ", "CRO", "MEI", 85, 72, 93, 172, 72,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/Luka_Modri%C4%87_2019.jpg/440px-Luka_Modri%C4%87_2019.jpg"),

    PlayerCard("salah", "MOHAMED SALAH", "EGY", "ATA", 92, 45, 90, 175, 89,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Mohamed_Salah_2022.jpg/440px-Mohamed_Salah_2022.jpg"),

    PlayerCard("rodri", "RODRI", "ESP", "MEI", 84, 93, 89, 191, 72,
        "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Manchester_City_vs._Inter_Milan_UEFA_Champions_League_Final_2023_Rodri.jpg/440px-Manchester_City_vs._Inter_Milan_UEFA_Champions_League_Final_2023_Rodri.jpg"),

    PlayerCard("bernardo", "BERNARDO SILVA", "POR", "MEI", 80, 68, 92, 173, 81,
        "https://images.unsplash.com/photo-1543351611-58f69d7c1781?w=500&auto=format&fit=crop"),

    PlayerCard("foden", "PHIL FODEN", "ENG", "ATA", 90, 56, 91, 171, 87,
        "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"),

    PlayerCard("odegaard", "MARTIN ØDEGAARD", "NOR", "MEI", 86, 62, 92, 178, 80,
        "https://images.unsplash.com/photo-1551958219-acbc608c6377?w=500&auto=format&fit=crop"),

    PlayerCard("saka", "BUKAYO SAKA", "ENG", "ATA", 88, 65, 89, 178, 90,
        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop"),

    PlayerCard("rice", "DECLAN RICE", "ENG", "MEI", 78, 90, 84, 185, 77,
        "https://images.unsplash.com/photo-1543351611-58f69d7c1781?w=500&auto=format&fit=crop"),

    PlayerCard("bruno_f", "BRUNO FERNANDES", "POR", "MEI", 89, 69, 90, 179, 81,
        "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"),

    PlayerCard("griezmann", "ANTOINE GRIEZMANN", "FRA", "ATA", 91, 72, 91, 176, 82,
        "https://images.unsplash.com/photo-1551958219-acbc608c6377?w=500&auto=format&fit=crop"),

    PlayerCard("lewandowski", "ROBERT LEWANDOWSKI", "POL", "ATA", 92, 44, 85, 185, 79,
        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop"),

    PlayerCard("lautaro", "LAUTARO MARTÍNEZ", "ARG", "ATA", 91, 48, 85, 174, 83,
        "https://images.unsplash.com/photo-1543351611-58f69d7c1781?w=500&auto=format&fit=crop"),

    PlayerCard("leao", "RAFAEL LEÃO", "POR", "ATA", 89, 36, 89, 188, 94,
        "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"),

    PlayerCard("son", "SON HEUNG-MIN", "KOR", "ATA", 90, 42, 86, 184, 89,
        "https://images.unsplash.com/photo-1551958219-acbc608c6377?w=500&auto=format&fit=crop"),

    PlayerCard("musiala", "JAMAL MUSIALA", "GER", "MEI", 87, 61, 93, 184, 85,
        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop"),

    PlayerCard("pedri", "PEDRI", "ESP", "MEI", 79, 68, 90, 174, 78,
        "https://images.unsplash.com/photo-1543351611-58f69d7c1781?w=500&auto=format&fit=crop"),

    PlayerCard("osimhen", "VICTOR OSIMHEN", "NGA", "ATA", 91, 41, 80, 185, 90,
        "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"),

    PlayerCard("valverde", "FEDERICO VALVERDE", "URU", "MEI", 86, 80, 85, 182, 88,
        "https://images.unsplash.com/photo-1551958219-acbc608c6377?w=500&auto=format&fit=crop"),

    PlayerCard("ruben_dias", "RÚBEN DIAS", "POR", "DEF", 50, 94, 80, 187, 70,
        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop"),

    PlayerCard("marquinhos", "MARQUINHOS", "BRA", "DEF", 54, 91, 82, 183, 79,
        "https://images.unsplash.com/photo-1543351611-58f69d7c1781?w=500&auto=format&fit=crop"),

    PlayerCard("hakimi", "ACHRAF HAKIMI", "MAR", "DEF", 82, 81, 84, 181, 92,
        "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop"),

    PlayerCard("davies", "ALPHONSO DAVIES", "CAN", "DEF", 81, 78, 82, 183, 96,
        "https://images.unsplash.com/photo-1551958219-acbc608c6377?w=500&auto=format&fit=crop"),

    PlayerCard("maignan", "MIKE MAIGNAN", "FRA", "GOL", 40, 89, 82, 191, 65,
        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop")
)

// Game ViewModel
class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // Firebase Realtime Database variables
    private var rtdbRoomRef: com.google.firebase.database.DatabaseReference? = null
    private var rtdbListener: com.google.firebase.database.ValueEventListener? = null
    var isOnlineCreator by mutableStateOf(false)
        private set
    var isMyTurn by mutableStateOf(true)
    var isWaitingForOpponent by mutableStateOf(false)
        private set

    // User Profile flow from Room Database
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile(username = "Jogador", points = 1420, wins = 12, losses = 8) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile(username = "Jogador", points = 1420, wins = 12, losses = 8)
        )

    var currentTab by mutableStateOf(GameTab.JOGAR)
        private set

    var playMode by mutableStateOf(PlayMode.NONE)
        private set

    // Active Card Battle states
    var playerCard by mutableStateOf<PlayerCard>(PLAYERS_DECK[0])
        private set

    var opponentCard by mutableStateOf<PlayerCard>(PLAYERS_DECK[1])
        private set

    var isComparing by mutableStateOf(false)
        private set

    var selectedStatName by mutableStateOf("")
        private set

    var matchResult by mutableStateOf<MatchResult?>(null)
        private set

    // Matchmaking variables
    var searchProgress by mutableStateOf(0f)
        private set

    var opponentName by mutableStateOf("")
        private set

    var roomCode by mutableStateOf("")
        private set

    init {
        // Initialize user profile in Database if not already present
        viewModelScope.launch {
            repository.getProfile()
        }
    }

    fun selectTab(tab: GameTab) {
        currentTab = tab
    }

    fun selectPlayMode(mode: PlayMode): String {
        if (mode == PlayMode.MATCHMAKING) {
            val code = "ST-${Random.nextInt(100000, 999999)}"
            createAndPublishOnlineRoom(code)
            return code
        } else if (mode == PlayMode.CPU_GAME) {
            dealCards()
            playMode = PlayMode.CPU_GAME
        } else {
            playMode = mode
        }
        return ""
    }

    fun joinRoomWithCode(code: String) {
        joinOnlineRoom(code)
    }

    private fun createAndPublishOnlineRoom(code: String) {
        roomCode = code
        isOnlineCreator = true
        isWaitingForOpponent = true
        playMode = PlayMode.MATCHMAKING
        searchProgress = 0f
        opponentName = ""
        isMyTurn = true

        val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://super-trunfo-da-copa-default-rtdb.firebaseio.com")
        val roomRef = rtdb.getReference("rooms").child(code)
        rtdbRoomRef = roomRef

        // Initial deck deal
        val list = PLAYERS_DECK.shuffled()
        val cCard = list[0]
        val oCard = list[1]

        val roomData = mapOf(
            "roomCode" to code,
            "creatorName" to userProfile.value.username,
            "joineeName" to "",
            "creatorCardId" to cCard.id,
            "joineeCardId" to oCard.id,
            "selectedStat" to "",
            "turn" to "CREATOR",
            "status" to "WAITING",
            "lastAction" to System.currentTimeMillis()
        )

        roomRef.setValue(roomData)
        listenToRoomChanges(roomRef)
    }

    private fun joinOnlineRoom(code: String) {
        roomCode = code
        isOnlineCreator = false
        isWaitingForOpponent = false
        playMode = PlayMode.MATCHMAKING
        searchProgress = 0.5f
        opponentName = ""
        isMyTurn = false

        val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://super-trunfo-da-copa-default-rtdb.firebaseio.com")
        val roomRef = rtdb.getReference("rooms").child(code)
        rtdbRoomRef = roomRef

        roomRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val status = snapshot.child("status").value as? String ?: "WAITING"
                if (status == "WAITING" || status == "PLAYING") {
                    val updates = mapOf(
                        "joineeName" to userProfile.value.username,
                        "status" to "PLAYING",
                        "lastAction" to System.currentTimeMillis()
                    )
                    roomRef.updateChildren(updates).addOnSuccessListener {
                        listenToRoomChanges(roomRef)
                    }
                } else {
                    dealCards()
                    playMode = PlayMode.CPU_GAME
                }
            } else {
                dealCards()
                playMode = PlayMode.CPU_GAME
            }
        }.addOnFailureListener {
            dealCards()
            playMode = PlayMode.CPU_GAME
        }
    }

    private fun listenToRoomChanges(ref: com.google.firebase.database.DatabaseReference) {
        rtdbListener?.let { ref.removeEventListener(it) }

        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                if (!snapshot.exists()) return

                val creatorNameRaw = snapshot.child("creatorName").value as? String ?: ""
                val joineeNameRaw = snapshot.child("joineeName").value as? String ?: ""
                val statusRaw = snapshot.child("status").value as? String ?: "WAITING"
                val creatorCardId = snapshot.child("creatorCardId").value as? String ?: ""
                val joineeCardId = snapshot.child("joineeCardId").value as? String ?: ""
                val selectedStatRaw = snapshot.child("selectedStat").value as? String ?: ""
                val turnRaw = snapshot.child("turn").value as? String ?: "CREATOR"

                if (isOnlineCreator) {
                    opponentName = joineeNameRaw
                    isMyTurn = turnRaw == "CREATOR"
                } else {
                    opponentName = creatorNameRaw
                    isMyTurn = turnRaw == "JOINEE"
                }

                val creatorCard = PLAYERS_DECK.find { it.id == creatorCardId } ?: PLAYERS_DECK[0]
                val joineeCard = PLAYERS_DECK.find { it.id == joineeCardId } ?: PLAYERS_DECK[1]

                if (isOnlineCreator) {
                    playerCard = creatorCard
                    opponentCard = joineeCard
                } else {
                    playerCard = joineeCard
                    opponentCard = creatorCard
                }

                if (statusRaw == "PLAYING") {
                    searchProgress = 1.0f
                    playMode = PlayMode.ONLINE_GAME
                    isWaitingForOpponent = false
                }

                if (selectedStatRaw.isNotEmpty() && !isComparing) {
                    val playerVal = getStatValue(playerCard, selectedStatRaw)
                    val opponentVal = getStatValue(opponentCard, selectedStatRaw)

                    selectedStatName = selectedStatRaw
                    isComparing = true

                    viewModelScope.launch {
                        delay(1500)

                        val result = when {
                            playerCard.isGold && !opponentCard.isGold -> MatchResult.WIN
                            opponentCard.isGold && !playerCard.isGold -> MatchResult.LOSS
                            playerVal > opponentVal -> MatchResult.WIN
                            playerVal < opponentVal -> MatchResult.LOSS
                            else -> if (Random.nextBoolean()) MatchResult.WIN else MatchResult.LOSS
                        }
                        matchResult = result

                        if (result == MatchResult.WIN) {
                            repository.recordWin()
                        } else {
                            repository.recordLoss()
                        }

                        delay(2800)
                        isComparing = false
                        matchResult = null
                        selectedStatName = ""

                        if (isOnlineCreator) {
                            val nextTurn = if (result == MatchResult.WIN) "CREATOR" else "JOINEE"
                            val list = PLAYERS_DECK.shuffled()
                            val nextCreatorCard = list[0]
                            val nextJoineeCard = list[1]

                            ref.updateChildren(
                                mapOf(
                                    "creatorCardId" to nextCreatorCard.id,
                                    "joineeCardId" to nextJoineeCard.id,
                                    "selectedStat" to "",
                                    "turn" to nextTurn,
                                    "lastAction" to System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }

        rtdbListener = listener
        ref.addValueEventListener(listener)
    }

    private fun getStatValue(card: PlayerCard, statName: String): Int {
        return when (statName) {
            "ATAQUE" -> card.attack
            "DEFESA" -> card.defense
            "TÉCNICA" -> card.technique
            "ALTURA" -> card.heightInCm
            "VELOCIDADE" -> card.speed
            else -> card.technique
        }
    }

    private fun dealCards() {
        val list = PLAYERS_DECK.shuffled()
        playerCard = list[0]
        opponentCard = list[1]
    }

    fun selectStat(statName: String, playerValue: Int, opponentValue: Int) {
        if (isComparing) return
        if (playMode == PlayMode.ONLINE_GAME) {
            if (!isMyTurn) return
            rtdbRoomRef?.child("selectedStat")?.setValue(statName)
        } else {
            selectedStatName = statName
            isComparing = true

            viewModelScope.launch {
                delay(1500)

                val result = when {
                    playerCard.isGold && !opponentCard.isGold -> MatchResult.WIN
                    opponentCard.isGold && !playerCard.isGold -> MatchResult.LOSS
                    playerValue > opponentValue -> MatchResult.WIN
                    playerValue < opponentValue -> MatchResult.LOSS
                    else -> if (Random.nextBoolean()) MatchResult.WIN else MatchResult.LOSS
                }
                matchResult = result

                if (result == MatchResult.WIN) {
                    repository.recordWin()
                } else {
                    repository.recordLoss()
                }

                delay(2800)
                isComparing = false
                matchResult = null
                selectedStatName = ""
                dealCards()
            }
        }
    }

    fun saveUsername(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val current = repository.getProfile()
            repository.saveProfile(current.copy(username = name.trim()))
        }
    }

    fun resetProfile() {
        viewModelScope.launch {
            repository.resetProfile()
        }
    }

    fun exitToMenu() {
        playMode = PlayMode.NONE
        isComparing = false
        matchResult = null
        selectedStatName = ""

        rtdbListener?.let { rtdbRoomRef?.removeEventListener(it) }
        rtdbListener = null
        rtdbRoomRef?.child("status")?.setValue("FINISHED")
        rtdbRoomRef = null
    }

    override fun onCleared() {
        super.onCleared()
        rtdbListener?.let { rtdbRoomRef?.removeEventListener(it) }
        rtdbListener = null
        rtdbRoomRef?.child("status")?.setValue("FINISHED")
        rtdbRoomRef = null
    }
}

// ViewModel Factory
class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:299941114533:android:ebf6c4c65bad6c90a443e7")
                    .setDatabaseUrl("https://super-trunfo-da-copa-default-rtdb.firebaseio.com")
                    .setProjectId("super-trunfo-da-copa")
                    .setApiKey("AIzaSyDummyApiKeyForRealtimeDatabase12345")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GameRepository(database.userDao())

        setContent {
            val gameViewModel: GameViewModel = viewModel(factory = GameViewModelFactory(repository))
            val profileState by gameViewModel.userProfile.collectAsState()

            val context = androidx.compose.ui.platform.LocalContext.current
            androidx.compose.runtime.LaunchedEffect(Unit) {
                val activity = context as? android.app.Activity
                activity?.intent?.data?.getQueryParameter("room")?.let { code ->
                    if (code.isNotEmpty() && gameViewModel.playMode == PlayMode.NONE) {
                        gameViewModel.joinRoomWithCode(code)
                    }
                }
            }

            CopaTrunfoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (gameViewModel.playMode == PlayMode.NONE) {
                            BottomNavBar(
                                currentTab = gameViewModel.currentTab,
                                onTabSelected = { gameViewModel.selectTab(it) }
                            )
                        }
                    },
                    containerColor = Color(0xFF081425)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header
                            HeaderBar(profileState.points)

                            // Main body based on selected tab
                            when (gameViewModel.currentTab) {
                                GameTab.JOGAR -> {
                                    JogarScreen(
                                        viewModel = gameViewModel,
                                        profile = profileState
                                    )
                                }
                                GameTab.RANKING -> {
                                    RankingScreen(
                                        userPoints = profileState.points,
                                        username = profileState.username
                                    )
                                }
                                GameTab.PERFIL -> {
                                    PerfilScreen(
                                        profile = profileState,
                                        onSaveName = { gameViewModel.saveUsername(it) },
                                        onReset = { gameViewModel.resetProfile() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Theme setup mimicking stadium atmosphere
@Composable
fun CopaTrunfoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFACC15),
            onPrimary = Color(0xFF3C2F00),
            surface = Color(0xFF081425),
            onSurface = Color(0xFFD8E3FB),
            secondary = Color(0xFF152031),
            onSecondary = Color(0xFFADB4CE)
        ),
        content = content
    )
}

// Top Bar Header Composable
@Composable
fun HeaderBar(points: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081425))
            .border(width = 1.dp, color = Color(0xFF1F2A3C))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Trophy icon",
                tint = Color(0xFFFACC15),
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "COPA 2026",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFFFACC15),
                letterSpacing = (-0.5).sp
            )
        }

        Box(
            modifier = Modifier
                .background(Color(0xFF1F2A3C), shape = RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF4D4632), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "PTS: %,d".format(points).replace(",", "."),
                color = Color(0xFFFACC15),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

// Player Card Custom Illustrative Art Engine
@Composable
fun PlayerAvatarDrawing(card: PlayerCard, modifier: Modifier = Modifier) {
    val imageResId = when (card.id) {
        "messi" -> R.drawable.img_card_messi
        "mbappe" -> R.drawable.img_card_mbappe
        "vini" -> R.drawable.img_card_vini
        "bellingham" -> R.drawable.img_card_bellingham
        "de_bruyne" -> R.drawable.img_card_de_bruyne
        "haaland" -> R.drawable.img_card_haaland
        "van_dijk" -> R.drawable.img_card_van_dijk
        "kane" -> R.drawable.img_card_kane
        "modric" -> R.drawable.img_card_modric
        "salah" -> R.drawable.img_card_salah
        // fallbacks to existing draws
        "pele" -> R.drawable.img_card_vini
        "ronaldo" -> R.drawable.img_card_mbappe
        "ronaldinho" -> R.drawable.img_card_salah
        "zico" -> R.drawable.img_card_bellingham
        "maradona" -> R.drawable.img_card_messi
        "cr7" -> R.drawable.img_card_kane
        "beckenbauer" -> R.drawable.img_card_van_dijk
        "garrincha" -> R.drawable.img_card_vini
        "distefano" -> R.drawable.img_card_de_bruyne
        else -> R.drawable.img_card_messi
    }

    val bgBrush = if (card.isGold) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFEF08A), Color(0xFFD97706))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        )
    }

    Box(
        modifier = modifier
            .background(bgBrush)
    ) {
        // Player illustrated sports card artwork
        val githubRawUrl = "https://raw.githubusercontent.com/davidaosp-creator/copa2026-super-trunfo/main/app/src/main/res/drawable/img_card_${card.id}.jpg"

        AsyncImage(
            model = githubRawUrl,
            contentDescription = card.name,
            placeholder = painterResource(id = imageResId),
            error = painterResource(id = imageResId),
            contentScale = ContentScale.Fit, // Entire player illustration is fully fitted inside card bounds
            modifier = Modifier.fillMaxSize()
        )

        // Super Trunfo red badge - matching the exact physical card layout
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .background(
                    color = if (card.isGold) Color(0xFFD97706) else Color(0xFFC8102E),
                    shape = RoundedCornerShape(4.dp)
                )
                .border(1.dp, Color(0xFFFEDF00), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (card.isGold) "SUPER TRUNFO ★" else "SUPER TRUNFO",
                color = Color.White,
                fontSize = if (card.isGold) 11.sp else 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        // Card ID identification in the bottom-right corner - matching the exact physical card layout
        val index = PLAYERS_DECK.indexOfFirst { it.id == card.id }
        val cardNumber = if (index >= 0) "%03d".format(index + 1) else "000"

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(
                    color = Color(0xFF152031).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(4.dp)
                )
                .border(1.dp, Color(0xFFFACC15).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = cardNumber,
                color = Color(0xFFFACC15),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// Bottom tab Navigation Bar
@Composable
fun BottomNavBar(currentTab: GameTab, onTabSelected: (GameTab) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF152031),
        tonalElevation = 8.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .height(76.dp)
            .border(BorderStroke(1.dp, Color(0xFF1F2A3C)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(
            selected = currentTab == GameTab.JOGAR,
            onClick = { onTabSelected(GameTab.JOGAR) },
            icon = {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Jogar tab"
                )
            },
            label = {
                Text(
                    "JOGAR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF081425),
                selectedTextColor = Color(0xFFFACC15),
                indicatorColor = Color(0xFFFACC15),
                unselectedIconColor = Color(0xFFD8E3FB).copy(alpha = 0.6f),
                unselectedTextColor = Color(0xFFD8E3FB).copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("play_tab")
        )

        NavigationBarItem(
            selected = currentTab == GameTab.RANKING,
            onClick = { onTabSelected(GameTab.RANKING) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Ranking tab"
                )
            },
            label = {
                Text(
                    "RANKING",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF081425),
                selectedTextColor = Color(0xFFFACC15),
                indicatorColor = Color(0xFFFACC15),
                unselectedIconColor = Color(0xFFD8E3FB).copy(alpha = 0.6f),
                unselectedTextColor = Color(0xFFD8E3FB).copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("ranking_tab")
        )

        NavigationBarItem(
            selected = currentTab == GameTab.PERFIL,
            onClick = { onTabSelected(GameTab.PERFIL) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil tab"
                )
            },
            label = {
                Text(
                    "PERFIL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF081425),
                selectedTextColor = Color(0xFFFACC15),
                indicatorColor = Color(0xFFFACC15),
                unselectedIconColor = Color(0xFFD8E3FB).copy(alpha = 0.6f),
                unselectedTextColor = Color(0xFFD8E3FB).copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("profile_tab")
        )
    }
}

// Helper to share invite link via WhatsApp/Standard Chooser
fun shareInviteLink(context: android.content.Context, roomCode: String) {
    val inviteText = "🏆 *DESAFIO SUPER TRUNFO DE CRAQUES* 🏆\n\n" +
            "Fala! Entre na minha sala privada para jogarmos uma partida de Super Trunfo agora mesmo!\n\n" +
            "👉 *Clique para ENTRAR na sala:* https://ais-pre-bxlh4j4xmmhngpgj4rovht-299941114533.us-west1.run.app?room=$roomCode\n\n" +
            "Colecione os melhores jogadores e vamos ver quem ganha! ⚽🔥"
    
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, inviteText)
        type = "text/plain"
    }
    
    val shareIntent = Intent.createChooser(sendIntent, "Convidar amigo via WhatsApp")
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(shareIntent)
}

// ---------------- JOGAR TAB SCREENS ----------------

@Composable
fun JogarScreen(viewModel: GameViewModel, profile: UserProfile) {
    val context = LocalContext.current
    AnimatedContent(
        targetState = viewModel.playMode,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "gameStateAnim"
    ) { mode ->
        when (mode) {
            PlayMode.NONE -> {
                MenuSelection(
                    onChooseCpu = { viewModel.selectPlayMode(PlayMode.CPU_GAME) },
                    onChooseOnline = {
                        val code = viewModel.selectPlayMode(PlayMode.MATCHMAKING)
                        shareInviteLink(context, code)
                    },
                    onJoinWithCode = { code ->
                        viewModel.joinRoomWithCode(code)
                    }
                )
            }
            PlayMode.MATCHMAKING -> {
                MatchmakingScreen(
                    progress = viewModel.searchProgress,
                    opponentName = viewModel.opponentName,
                    roomCode = viewModel.roomCode,
                    onInviteClick = {
                        shareInviteLink(context, viewModel.roomCode)
                    }
                )
            }
            PlayMode.CPU_GAME, PlayMode.ONLINE_GAME -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CardBattleScreen(
                        playerCard = viewModel.playerCard,
                        opponentCard = viewModel.opponentCard,
                        isOnline = viewModel.playMode == PlayMode.ONLINE_GAME,
                        opponentName = if (viewModel.playMode == PlayMode.ONLINE_GAME) viewModel.opponentName else "CPU 🤖",
                        isMyTurn = if (viewModel.playMode == PlayMode.ONLINE_GAME) viewModel.isMyTurn else true,
                        onSelectStat = { stat, valP, valO -> viewModel.selectStat(stat, valP, valO) },
                        onExit = { viewModel.exitToMenu() }
                    )
                    ConfrontOverlay(
                        isComparing = viewModel.isComparing,
                        statName = viewModel.selectedStatName,
                        playerCard = viewModel.playerCard,
                        opponentCard = viewModel.opponentCard,
                        result = viewModel.matchResult
                    )
                }
            }
        }
    }
}

// Selection list of play modes
@Composable
fun MenuSelection(
    onChooseCpu: () -> Unit,
    onChooseOnline: () -> Unit,
    onJoinWithCode: (String) -> Unit
) {
    var codeText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ESCOLHA SEU DESAFIO",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD8E3FB),
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .background(Color(0xFFFACC15), shape = RoundedCornerShape(2.dp))
                .padding(bottom = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CPU Play Option Card
        Button(
            onClick = onChooseCpu,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .testTag("vs_cpu_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACC15)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF3C2F00).copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "CPU icon",
                            tint = Color(0xFF3C2F00),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "VERSUS CPU",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF3C2F00)
                    )
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Go play option",
                    tint = Color(0xFF3C2F00)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Simulated online match option card
        Button(
            onClick = onChooseOnline,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .testTag("vs_online_button")
                .border(2.dp, Color(0xFF9A9078), RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF152031)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFACC15).copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Online icon",
                            tint = Color(0xFFFACC15),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "AMIGO ONLINE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFFACC15)
                    )
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Go play",
                    tint = Color(0xFFFACC15)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Manual Join section
        Text(
            text = "OU ENTRE EM UMA SALA EXISTENTE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD8E3FB).copy(alpha = 0.5f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = codeText,
                onValueChange = { codeText = it.uppercase() },
                placeholder = { 
                    Text(
                        text = "Código (ex: ST-123456)", 
                        color = Color(0xFFD8E3FB).copy(alpha = 0.4f), 
                        fontSize = 14.sp
                    ) 
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFACC15),
                    unfocusedBorderColor = Color(0xFF1F2A3C),
                    focusedTextColor = Color(0xFFD8E3FB),
                    unfocusedTextColor = Color(0xFFD8E3FB),
                    focusedPlaceholderColor = Color(0xFFD8E3FB).copy(alpha = 0.4f),
                    unfocusedPlaceholderColor = Color(0xFFD8E3FB).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("manual_room_code_input")
            )

            Button(
                onClick = {
                    if (codeText.isNotBlank()) {
                        onJoinWithCode(codeText.trim())
                    }
                },
                enabled = codeText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFACC15),
                    disabledContainerColor = Color(0xFF1F2A3C)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("manual_join_button")
            ) {
                Text(
                    text = "ENTRAR",
                    fontWeight = FontWeight.Bold,
                    color = if (codeText.isNotBlank()) Color(0xFF3C2F00) else Color(0xFFD8E3FB).copy(alpha = 0.4f)
                )
            }
        }
    }
}

// Simulated Matchmaking loader
@Composable
fun MatchmakingScreen(
    progress: Float,
    opponentName: String,
    roomCode: String,
    onInviteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .border(1.dp, Color(0xFF1F2A3C), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF152031)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color(0xFFFACC15),
                    modifier = Modifier.size(36.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "SALA PRIVADA CRIADA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD8E3FB).copy(alpha = 0.6f),
                    letterSpacing = 1.5.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = roomCode,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFACC15),
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Compartilhe o link abaixo com seu amigo via WhatsApp para jogarem juntos simultaneamente!",
                    fontSize = 12.sp,
                    color = Color(0xFFD8E3FB).copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // WhatsApp-green share button
                Button(
                    onClick = onInviteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("whatsapp_share_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share on WhatsApp icon",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "CONVIDAR NO WHATSAPP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (opponentName.isEmpty()) "AGUARDANDO SEU AMIGO CONECTAR..." else "AMIGO CONECTADO!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD8E3FB),
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (opponentName.isNotEmpty()) Color(0xFF4ADE80) else Color(0xFFFACC15),
            trackColor = Color(0xFF1F2A3C)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (opponentName.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF25D366).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF25D366), RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "✓ Jogador $opponentName entrou na sala!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4ADE80)
                )
            }
        } else {
            Text(
                text = "Simulando entrada simultânea...",
                fontSize = 11.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFFD8E3FB).copy(alpha = 0.5f)
            )
        }
    }
}

// Active Match card battle setup
@Composable
fun CardBattleScreen(
    playerCard: PlayerCard,
    opponentCard: PlayerCard,
    isOnline: Boolean,
    opponentName: String,
    isMyTurn: Boolean = true,
    onSelectStat: (statName: String, playerVal: Int, opponentVal: Int) -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Option to leave
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isOnline) "PARTIDA: MODO ONLINE" else "PARTIDA: VERSUS CPU",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9A9078)
            )

            Row(
                modifier = Modifier
                    .clickable { onExit() }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Leave game",
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "DESISTIR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF87171)
                )
            }
        }

        if (isOnline) {
            Text(
                text = if (isMyTurn) "✓ SUA VEZ! ESCOLHA UM ATRIBUTO DO SEU CARD" else "⏳ AGUARDANDO PALPITE DO ADVERSÁRIO...",
                color = if (isMyTurn) Color(0xFF4ADE80) else Color(0xFFFB923C),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Scrollable column just in case sizes are cramped on older devices
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Player Card Section
                item {
                    InteractivePlayerCard(
                        card = playerCard,
                        isMyTurn = isMyTurn,
                        onSelectStat = { stat, value ->
                            val oppValue = when (stat) {
                                "ATAQUE" -> opponentCard.attack
                                "DEFESA" -> opponentCard.defense
                                "TÉCNICA" -> opponentCard.technique
                                "ALTURA" -> opponentCard.heightInCm
                                "VELOCIDADE" -> opponentCard.speed
                                else -> opponentCard.technique
                            }
                            onSelectStat(stat, value, oppValue)
                        }
                    )
                }

                // VS circle in between
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFFACC15), CircleShape)
                                .border(2.dp, Color(0xFF081425), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "VS",
                                color = Color(0xFF3C2F00),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }

                // Opponent Card Section
                item {
                    OpponentCardPlaceholder(
                        card = opponentCard,
                        opponentLabel = opponentName
                    )
                }
            }
        }
    }
}

// User-controlled playing card
@Composable
fun InteractivePlayerCard(
    card: PlayerCard,
    isMyTurn: Boolean = true,
    onSelectStat: (statName: String, value: Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (card.isGold) BorderStroke(4.dp, Brush.verticalGradient(listOf(Color(0xFFFEF08A), Color(0xFFD97706))))
                else BorderStroke(2.dp, Color(0xFF1F2A3C)),
                RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2A3C)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (card.isGold) Color(0xFFFEDF00) else Color(0xFF152031))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(if (card.isGold) Color(0xFF3C2F00) else Color(0xFF1F2A3C), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = card.country,
                            color = if (card.isGold) Color(0xFFFEDF00) else Color(0xFFD8E3FB),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Text(
                        text = card.name,
                        color = if (card.isGold) Color(0xFF3C2F00) else Color(0xFFD8E3FB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(if (card.isGold) Color(0xFF3C2F00).copy(alpha = 0.1f) else Color(0xFFD8E3FB).copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .border(1.dp, if (card.isGold) Color(0xFF3C2F00).copy(alpha = 0.2f) else Color(0xFFD8E3FB).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = card.position,
                        color = if (card.isGold) Color(0xFF3C2F00) else Color(0xFFD8E3FB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Image container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                PlayerAvatarDrawing(
                    card = card,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Legend Bio explanation
            if (card.isGold && card.legendBio.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF9C3).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFFEDF00).copy(alpha = 0.3f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Gold Crown",
                                tint = Color(0xFFFEDF00),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "LENDA DO SUPER TRUNFO",
                                color = Color(0xFFFEDF00),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = card.legendBio,
                            color = Color(0xFFFEF9C3),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Options stats selection
            Column(
                modifier = Modifier
                    .background(Color(0xFF1F2A3C))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isMyTurn) "TOQUE PARA JOGAR:" else "CO-PILOTO: SUA VEZ NO PRÓXIMO TURNO!",
                    color = if (isMyTurn) Color(0xFF9A9078) else Color(0xFFFB923C),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                StatSelectionRow(
                    label = "ATAQUE",
                    value = card.attack,
                    icon = Icons.Default.PlayArrow,
                    onClick = { if (isMyTurn) onSelectStat("ATAQUE", card.attack) },
                    modifier = Modifier.testTag("stat_row_attack")
                )

                StatSelectionRow(
                    label = "DEFESA",
                    value = card.defense,
                    icon = Icons.Default.Build,
                    onClick = { if (isMyTurn) onSelectStat("DEFESA", card.defense) },
                    modifier = Modifier.testTag("stat_row_defense")
                )

                StatSelectionRow(
                    label = "TÉCNICA",
                    value = card.technique,
                    icon = Icons.Default.Star,
                    onClick = { if (isMyTurn) onSelectStat("TÉCNICA", card.technique) },
                    modifier = Modifier.testTag("stat_row_technique")
                )

                StatSelectionRow(
                    label = "ALTURA",
                    value = card.heightInCm,
                    customValueString = "%.2f m".format(card.heightInCm / 100.0),
                    icon = Icons.Default.Person,
                    onClick = { if (isMyTurn) onSelectStat("ALTURA", card.heightInCm) },
                    modifier = Modifier.testTag("stat_row_height")
                )

                StatSelectionRow(
                    label = "VELOCIDADE",
                    value = card.speed,
                    icon = Icons.Default.ArrowForward,
                    onClick = { if (isMyTurn) onSelectStat("VELOCIDADE", card.speed) },
                    modifier = Modifier.testTag("stat_row_speed")
                )
            }
        }
    }
}

// Stat Row component
@Composable
fun StatSelectionRow(
    label: String,
    value: Int,
    customValueString: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector, // removido imageVector
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) Color(0xFFFACC15) else Color(0xFF2A3548))
            .pointerInput(Unit) {  // corrigido isPressed
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$label icon",
                tint = if (isPressed) Color(0xFF3C2F00) else Color(0xFFFACC15),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPressed) Color(0xFF3C2F00) else Color(0xFFD8E3FB)
            )
        }
        Text(
            text = customValueString ?: value.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) Color(0xFF3C2F00) else Color(0xFFFACC15)
        )
    }
}

// Blurry Opponent card placeholder
@Composable
fun OpponentCardPlaceholder(card: PlayerCard, opponentLabel: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (card.isGold) BorderStroke(3.dp, Color(0xFFD97706).copy(alpha = 0.6f))
                else BorderStroke(2.dp, Color(0xFF1F2A3C)),
                RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152031)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A3548))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF152031), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = card.country,
                            color = Color(0xFFD8E3FB),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Text(
                        text = card.name,
                        color = Color(0xFFD8E3FB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFD8E3FB).copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = card.position,
                        color = Color(0xFFD8E3FB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Blurry image container to mimic unknown card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                PlayerAvatarDrawing(
                    card = card,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF081425).copy(alpha = 0.5f))
                )

                // Overlay badge
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Hidden card icon",
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = opponentLabel.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFACC15),
                        letterSpacing = 1.sp
                    )
                }
            }

            // Disabled stats showing ??
            Column(
                modifier = Modifier
                    .background(Color(0xFF111C2D))
                    .padding(16.dp)
                    .blur(3.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2A3C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF9A9078),
                            modifier = Modifier.size(20.dp)
                        )
                        Text("ATAQUE", fontWeight = FontWeight.Bold, color = Color(0xFF9A9078))
                    }
                    Text("??", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A9078))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2A3C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color(0xFF9A9078),
                            modifier = Modifier.size(20.dp)
                        )
                        Text("DEFESA", fontWeight = FontWeight.Bold, color = Color(0xFF9A9078))
                    }
                    Text("??", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A9078))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2A3C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF9A9078),
                            modifier = Modifier.size(20.dp)
                        )
                        Text("TÉCNICA", fontWeight = FontWeight.Bold, color = Color(0xFF9A9078))
                    }
                    Text("??", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A9078))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2A3C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF9A9078),
                            modifier = Modifier.size(20.dp)
                        )
                        Text("ALTURA", fontWeight = FontWeight.Bold, color = Color(0xFF9A9078))
                    }
                    Text("??", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A9078))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2A3C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF9A9078),
                            modifier = Modifier.size(20.dp)
                        )
                        Text("VELOCIDADE", fontWeight = FontWeight.Bold, color = Color(0xFF9A9078))
                    }
                    Text("??", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A9078))
                }
            }
        }
    }
}

// Direct comparison loader and result screens
@Composable
fun ConfrontOverlay(
    isComparing: Boolean,
    statName: String,
    playerCard: PlayerCard,
    opponentCard: PlayerCard,
    result: MatchResult?
) {
    if (isComparing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF081425).copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                if (result == null) {
                    // Loading State
                    Text(
                        text = "CONFRONTANDO...",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFFFACC15),
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )

                    CircularProgressIndicator(
                        color = Color(0xFFFACC15),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(64.dp)
                    )

                    Text(
                        text = "Comparando atributo: $statName",
                        fontSize = 16.sp,
                        color = Color(0xFFD8E3FB),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Result State
                    val isWin = result == MatchResult.WIN
                    val isSuperWin = (playerCard.isGold && !opponentCard.isGold) && isWin
                    val isSuperLoss = (opponentCard.isGold && !playerCard.isGold) && !isWin

                    val playerVal = when (statName) {
                        "ATAQUE" -> playerCard.attack
                        "DEFESA" -> playerCard.defense
                        "TÉCNICA" -> playerCard.technique
                        "ALTURA" -> playerCard.heightInCm
                        "VELOCIDADE" -> playerCard.speed
                        else -> playerCard.technique
                    }
                    val oppVal = when (statName) {
                        "ATAQUE" -> opponentCard.attack
                        "DEFESA" -> opponentCard.defense
                        "TÉCNICA" -> opponentCard.technique
                        "ALTURA" -> opponentCard.heightInCm
                        "VELOCIDADE" -> opponentCard.speed
                        else -> opponentCard.technique
                    }

                    val playerValStr = if (statName == "ALTURA") "%.2f m".format(playerVal / 100.0) else "$playerVal"
                    val oppValStr = if (statName == "ALTURA") "%.2f m".format(oppVal / 100.0) else "$oppVal"

                    Icon(
                        imageVector = if (isSuperWin || isSuperLoss) Icons.Default.Star else (if (isWin) Icons.Default.Done else Icons.Default.Warning),
                        contentDescription = "Outcome icon",
                        tint = if (isSuperWin) Color(0xFFFACC15) else if (isWin) Color(0xFF4ADE80) else Color(0xFFF87171),
                        modifier = Modifier
                            .size(96.dp)
                            .border(
                                3.dp,
                                if (isSuperWin) Color(0xFFFACC15) else if (isWin) Color(0xFF4ADE80) else Color(0xFFF87171),
                                CircleShape
                            )
                            .padding(16.dp)
                    )

                    Text(
                        text = if (isSuperWin) "SUPER TRUNFO! ★" else if (isSuperLoss) "SUPER TRUNFO INIMIGO!" else if (isWin) "VITÓRIA!" else "DERROTA",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = if (isSuperWin) Color(0xFFFACC15) else if (isWin) Color(0xFF4ADE80) else Color(0xFFF87171),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = if (isSuperWin) {
                            "Vitória instantânea! Sua lenda ${playerCard.name} dominou o campo!"
                        } else if (isSuperLoss) {
                            "Derrota automática! O oponente tinha a lenda dourada ${opponentCard.name}!"
                        } else if (isWin) {
                            "Você venceu no atributo $statName!"
                        } else {
                            "O oponente levou a melhor no atributo $statName."
                        },
                        fontSize = 16.sp,
                        color = Color(0xFFD8E3FB),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    // Cards comparisons detail
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF152031), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF1F2A3C), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "SEU ATRIBUTO",
                                fontSize = 11.sp,
                                color = Color(0xFF9A9078),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = playerValStr,
                                fontSize = 28.sp,
                                color = if (isWin) Color(0xFF4ADE80) else Color(0xFFD8E3FB),
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = playerCard.name.split(" ").last(),
                                fontSize = 12.sp,
                                color = Color(0xFFD8E3FB)
                            )
                        }

                        Text(
                            "vs",
                            fontSize = 16.sp,
                            color = Color(0xFFFACC15),
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "OPONENTE",
                                fontSize = 11.sp,
                                color = Color(0xFF9A9078),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = oppValStr,
                                fontSize = 28.sp,
                                color = if (!isWin) Color(0xFFF87171) else Color(0xFFD8E3FB),
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = opponentCard.name.split(" ").last(),
                                fontSize = 12.sp,
                                color = Color(0xFFD8E3FB)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- RANKING TAB SCREENS ----------------

data class LeaderboardItem(val rank: Int, val name: String, val points: Int, val isUser: Boolean = false)

@Composable
fun RankingScreen(userPoints: Int, username: String) {
    // Generate dynamic ranking based on active points
    val rankingItems = remember(userPoints, username) {
        val list = mutableListOf(
            LeaderboardItem(1, "LIONEL MESSI", 1980),
            LeaderboardItem(2, "CRISTIANO RONALDO", 1850),
            LeaderboardItem(3, "NEYMAR JR", 1680),
            LeaderboardItem(4, "KYLIAN MBAPPÉ", 1550),
            LeaderboardItem(5, "VINI JR.", 1480),
            LeaderboardItem(6, "HAALAND_BOT", 1400),
            LeaderboardItem(7, "JUDE_B", 1320),
            LeaderboardItem(8, "DEBRUYNE_MVP", 1250),
            LeaderboardItem(9, "KANE_ENG", 1100),
            LeaderboardItem(10, "MODRIC_GOD", 950)
        )

        // Insert user appropriately
        val userItem = LeaderboardItem(0, username.uppercase(), userPoints, true)
        list.add(userItem)

        // Sort desc
        list.sortByDescending { it.points }

        // Reassign clean rank sequence
        list.mapIndexed { idx, item ->
            item.copy(rank = idx + 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "CLASSIFICAÇÃO COPA '26",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD8E3FB),
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            "Veja quem são os maiores pontuadores do campeonato.",
            fontSize = 13.sp,
            color = Color(0xFF9A9078),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(rankingItems) { _, item ->
                val bgGradient = if (item.isUser) {
                    Brush.horizontalGradient(colors = listOf(Color(0xFF3C2F00), Color(0xFF152031)))
                } else {
                    Brush.horizontalGradient(colors = listOf(Color(0xFF152031), Color(0xFF152031)))
                }

                val borderStroke = if (item.isUser) {
                    BorderStroke(2.dp, Color(0xFFFACC15))
                } else {
                    BorderStroke(1.dp, Color(0xFF1F2A3C))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = borderStroke
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgGradient)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank number icon bubble
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        when (item.rank) {
                                            1 -> Color(0xFFFACC15)
                                            2 -> Color(0xFFBEC6E0)
                                            3 -> Color(0xFFCD7F32)
                                            else -> Color(0xFF1F2A3C)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${item.rank}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = when (item.rank) {
                                        1 -> Color(0xFF3C2F00)
                                        2 -> Color(0xFF283044)
                                        3 -> Color(0xFFFFFFFF)
                                        else -> Color(0xFFD8E3FB)
                                    }
                                )
                            }

                            Text(
                                text = item.name,
                                fontWeight = if (item.isUser) FontWeight.ExtraBold else FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (item.isUser) Color(0xFFFACC15) else Color(0xFFD8E3FB)
                            )
                        }

                        Text(
                            text = "${item.points} PTS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (item.isUser) Color(0xFFFACC15) else Color(0xFFD8E3FB)
                        )
                    }
                }
            }
        }
    }
}

// ---------------- PERFIL TAB SCREENS ----------------

@Composable
fun PerfilScreen(
    profile: UserProfile,
    onSaveName: (String) -> Unit,
    onReset: () -> Unit
) {
    var textVal by remember(profile.username) { mutableStateOf(profile.username) }
    val focusManager = LocalFocusManager.current

    val totalGames = profile.wins + profile.losses
    val winRate = if (totalGames == 0) 0 else (profile.wins * 100) / totalGames

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Identity card item
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF152031)),
                border = BorderStroke(1.dp, Color(0xFF1F2A3C)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile picture placeholder
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFF1F2A3C), CircleShape)
                            .border(2.dp, Color(0xFFFACC15), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar icon",
                            tint = Color(0xFFFACC15),
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    // Username inputs with edit
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "NOME DE USUÁRIO:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9A9078)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = textVal,
                                onValueChange = { textVal = it },
                                textStyle = TextStyle(
                                    color = Color(0xFFD8E3FB),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(Color(0xFFFACC15)),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF1F2A3C), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("username_text_field"),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        onSaveName(textVal)
                                        focusManager.clearFocus()
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    onSaveName(textVal)
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier
                                    .background(Color(0xFFFACC15), RoundedCornerShape(8.dp))
                                    .size(38.dp)
                                    .testTag("save_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save profile",
                                    tint = Color(0xFF3C2F00),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stats boxes grid
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ESTATÍSTICAS DA CONTA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF9A9078),
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCardBox(
                        label = "VITÓRIAS",
                        value = "${profile.wins}",
                        color = Color(0xFF4ADE80),
                        modifier = Modifier.weight(1f)
                    )
                    StatCardBox(
                        label = "DERROTAS",
                        value = "${profile.losses}",
                        color = Color(0xFFF87171),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCardBox(
                        label = "PARTIDAS",
                        value = "$totalGames",
                        color = Color(0xFFD8E3FB),
                        modifier = Modifier.weight(1f)
                    )
                    StatCardBox(
                        label = "APROVEITAMENTO",
                        value = "$winRate%",
                        color = Color(0xFFFACC15),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Card Collection Progress
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF152031)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF1F2A3C))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "COLEÇÃO DE CARTAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9A9078)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "10 / 10 Desbloqueadas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD8E3FB)
                        )
                        Text(
                            text = "100%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFACC15)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { 1.0f },
                        color = Color(0xFFFACC15),
                        trackColor = Color(0xFF1F2A3C),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        // Reset game profile buttons
        item {
            Button(
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_profile_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF93000A).copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, Color(0xFF93000A)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "RESETAR PROGRESSO",
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Stats helper component
@Composable
fun StatCardBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152031)),
        border = BorderStroke(1.dp, Color(0xFF1F2A3C)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF9A9078),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}
