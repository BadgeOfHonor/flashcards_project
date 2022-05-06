package flashcards

import flashcards.FlashCards.Loger.inputln
import flashcards.FlashCards.Loger.saveLogToFile
import flashcards.FlashCards.Loger.writeln
import java.io.File

fun main(args: Array<String>) {
    val play = FlashCards()
    for (i in args.indices) {
        when (args[i]) {
            "-import" -> if (i != args.lastIndex) play.importStart = args[i + 1]
            "-export" -> if (i != args.lastIndex) play.exportEnd = args[i + 1]
        }
    }
    play.run()
}

class FlashCards {
    var card: Card = Card("", "")
        set(value) {
            field = value
            term = card.term
            definition = card.definition
        }
    var term: String = card.term
    var definition: String = card.definition
    var save = false
    var importStart: String = ""
        set(value) {
            field = value
            if (value != "") importCardsToStart()
        }
    var exportEnd: String = ""


    fun importCardsToStart() {
        val fileName = importStart
        if (File(fileName).exists()) {
            writeln("${importCardsToDeck(fileName).size} cards have been loaded.")
        } else writeln("File not found.")
    }

    fun exportCardsToEnd() {
        val fileName = exportEnd
        if (fileName != "") {
            exportCardsFromDeck(save, fileName)
            writeln("${deck.size} cards have been saved.")
        }
    }

    fun run() {
        while (true) {
            writeln("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
            val userResponse = inputln().lowercase()
            when (userResponse) {
                "add" -> { creatNewCard() }
                "remove" -> { removeCard() }
                "import" -> { importCards() }
                "export" -> { exportCards() }
                "ask" -> { askCards() }
                "exit" -> { exportCardsToEnd(); println("Bye bye!"); return }
                "log" -> { loger() }
                "hardest card" -> { hardestCard() }
                "reset stats" -> { resStats() }
            }
            println()
        }
    }

    fun creatNewCard() {
        writeln("The card:")
        val term: String = readln()
        if (Card(term = term).isNotUnic()) {
            writeln("The card \"$term\" already exists.")
            return
        }
        writeln("The definition of the card:")
        val definition: String = inputln()
        if (Card(definition = definition).isNotUnic()) {
            writeln("The definition \"$definition\" already exists.")
                return
            }
        Card(term, definition).addToDeck()
        writeln("The pair (\"$term\":\"$definition\") has been added")
    }

    fun removeCard() {
        writeln("Which card?")
        val _term = inputln()
        if(Card(term = _term).isNotUnic()) {
            _term.removeFromDeck()
            writeln("The card has been removed.")
        } else writeln("Can't remove \"$_term\": there is no such card.")
    }

    fun importCards() {
        writeln("File name:")
        val fileName = inputln()
        if (File(fileName).exists()) {
            writeln("${importCardsToDeck(fileName).size} cards have been loaded.")
        } else writeln("File not found.")
    }

    fun exportCards() {
        writeln("File name:")
        val fileName = inputln()
        exportCardsFromDeck(save, fileName)
        writeln("${deck.size} cards have been saved.")
    }

    fun askCards() {

        fun String.check() {
            writeln(if (this == card.definition) "Correct!" else {
                card.mistakes++
                val _card = Card(definition = this).findCard()
                if (_card == null) {
                    "Wrong. The right answer is \"${card.definition}\"."
                } else {
                    "Wrong. The right answer is \"${card.definition}\", but your definition is correct for \"${_card!!.term}\"."
                }
            })
        }

        writeln("How many times to ask?")
        for (i in 1..inputln().toInt()) {
            card = askCardFromDeck()
            writeln("Print the definition of \"$term\":")
            inputln().check()
        }
    }

    fun loger() {
        writeln("File name:")
        val fileName = inputln()
        saveLogToFile(fileName)
        writeln("The log has been saved.")
    }

    fun hardestCard() {
        val maxMist = maxMistake()
        if (maxMist == null || maxMist == 0) writeln("There are no cards with errors.") else {
            val hardestMistake = findHardest(maxMist)
            if (hardestMistake!!.size > 1) {
                var strTerm = ""
                for (i in hardestMistake.indices) {
                    strTerm += "\"${hardestMistake[i].term}\""
                     if (i != hardestMistake.lastIndex) strTerm += ", "
                }
                writeln("The hardest cards are $strTerm. You have $maxMist errors answering them.")
            } else writeln("The hardest card is \"${hardestMistake.first().term}\". You have $maxMist errors answering it")
        }
    }

    fun resStats() {
        resetStats()
        writeln("Card statistics have been reset.")
    }

    companion object Deck {
        var deck = HashSet<Card>()

        fun String.addToDeck(definition: String) = deck.add(Card(this, definition))

        fun Card.addToDeck() = deck.add(this@addToDeck)

        fun Card.removeFromDeck() = deck.removeIf { it == this@removeFromDeck }

        fun String.removeFromDeck() = Card(this@removeFromDeck, this@removeFromDeck).removeFromDeck()

        fun importCardsToDeck(fileName: String): Set<Card> {
            val subDeck = HashSet<Card>()
            File(fileName).forEachLine { it.split(" : ").let { i -> subDeck.add(Card(i[0], i[1], i.last().toInt())) } }
            deck.interDelCard(subDeck)
            deck += subDeck
            return subDeck
        }

        fun HashSet<Card>.interDelCard(nother: HashSet<Card>) {
            for (i in nother) {
                this.removeIf { it == i }
            }
        }

        fun exportCardsFromDeckWithSave(fileName: String) {
            val subDeck = HashSet<Card>()
            if (File(fileName).exists()) {
                File(fileName).forEachLine { it.split(" : ").let { i -> subDeck.add(Card(i.first(), i.last())) } }
                subDeck.interDelCard(deck)
            }
            subDeck += deck
            File(fileName).writeText("")
            for (i in subDeck) {
                File(fileName).appendText(i.toString())
            }
        }

        fun exportCardsFromDeckOutSave(fileName: String) {
            File(fileName).writeText("")
            for (i in deck) {
                File(fileName).appendText(i.toString())
            }
        }

        fun exportCardsFromDeck(_save: Boolean, _fileName: String) {
            if (_save) exportCardsFromDeckWithSave(_fileName) else exportCardsFromDeckOutSave(_fileName)
        }

        fun Card.findCard(): Card? {
            for (i in deck) {
                if (this == i) return i
            }
            return null
        }

        fun Card.isNotUnic(): Boolean = this.findCard() != null

        fun askCardFromDeck(): Card = deck.random()

        fun maxMistake(): Int? = deck.maxOfOrNull { item -> item.mistakes }

        fun findHardest(numMistake: Int? = maxMistake()): List<Card>? {
            if (numMistake == null) return null
            return deck.filter { it.mistakes == numMistake }
        }

        fun resetStats() = run { for (i in deck) { i.mistakes = 0 } }

        data class Card(val term: String = "", val definition: String = "", var mistakes: Int = 0) {

            override fun toString(): String = "$term : $definition : $mistakes\n"

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                other as Card
                return term == other.term || definition == other.definition
            }

            override fun hashCode(): Int {
                var result = term.hashCode()
                result = 31 * result + definition.hashCode()
                return result
            }
        }
    }

    object Loger {
        val logProgram = mutableListOf<Log>()

        fun writeln(str: String) {
            logProgram.add(Log(str))
            println(str)
        }

        fun inputln(): String {
            val str = readln()
            logProgram.add(Log(str))
            return str
        }

        fun saveLogToFile(_fileName: String) {
            File(_fileName).writeText("")
            for (i in logProgram) {
                File(_fileName).appendText("$i\n")
            }
        }

        data class Log(val msg: String)
    }
}
