package org.example

import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.util.ISeq
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.sqrt
import kotlin.random.Random

fun main() {
    println("test1")
    // deklaracja miast - tu random

    val cities = List(20) { City(Random.nextDouble(0.0, 100.0).toRoundedBigDecimal(3), Random.nextDouble(0.0, 100.0).toRoundedBigDecimal(3)) }

    // Funkcja fitness minimalizuje calkowity dystans
    val fitnessFunction: (Genotype<IntegerGene>) -> Double = { genotype ->
        val order = genotype.chromosome().asSequence().map { it.allele() }.toList()
        val orderedCities = order.map { cities[it] }
        totalDistance(orderedCities)
    }

    //genotyp - permutacja liczb od 0 do 19
    val genotypeFactory = Genotype.of(IntegerChromosome.of(0, cities.size - 1, cities.size))

    // Tworzenie silnika ewolucji, deklaracja populacji i pziom mutacji
    val engine = Engine.builder(fitnessFunction, genotypeFactory)
        .minimizing()
        .populationSize(500)
        .alterers(SwapMutator<IntegerGene, Double>(0.2))
        .build()

    // Uruchomienie ewolucji, liczba pokolen
    val bestGenotype = engine.stream()
        .limit(200)
        .collect(EvolutionResult.toBestGenotype())

    // Wyświetlenie najlepszego rozwiązania
    val bestOrder = bestGenotype.chromosome().asSequence().map { it.allele() }.toList()
    val bestRoute = bestOrder.map { cities[it] }
    println("Best Route: $bestRoute")
    println("Distance: ${totalDistance(bestRoute)}")
}

data class City(val x: Double, val y: Double) {
    // dystans do innego miasta
    fun distanceTo(other: City): Double {
        return sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)).toRoundedBigDecimal(3)
    }
}

fun Double.toRoundedBigDecimal(scale: Int): Double {
    return BigDecimal(this).setScale(scale, RoundingMode.HALF_UP).toDouble()
}

// Funkcja obliczająca całkowity dystans trasy (permucji miast)
fun totalDistance(cities: List<City>): Double {
    var distance = 0.0
    for (i in 0 until cities.size - 1) {
        distance += cities[i].distanceTo(cities[i + 1])
    }
    // Powrót do miasta początkowego
    distance += cities.last().distanceTo(cities.first())
    return distance
}
//