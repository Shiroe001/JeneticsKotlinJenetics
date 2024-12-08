package org.example

import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.util.ISeq
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.markers.SeriesMarkers
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

fun main() {
    val cities = listOf(
        City(28.482, 47.081), City(2.954, 65.231), City(57.661, 41.342), City(16.218, 26.652),
        City(1.362, 21.920), City(74.200, 62.515), City(1.500, 96.906), City(39.273, 16.866),
        City(62.511, 43.700), City(50.556, 90.515), City(2.461, 54.216), City(32.365, 81.657),
        City(80.751, 83.219), City(91.801, 46.936), City(34.234, 74.501), City(25.525, 52.994),
        City(4.058, 2.696), City(97.129, 83.323), City(98.254, 16.345)
    )

    // Define the fitness function
    val fitnessFunction: (Genotype<*>) -> Double = { genotype ->
        val chromosome = genotype.chromosome() as PermutationChromosome<Int>
        val order = chromosome.stream().map { it.allele() }.toList()
        val orderedCities = order.map { cities[it] }
        totalDistance(orderedCities)
    }

    // Create a genotype factory for permutations
    val genotypeFactory = Genotype.of(PermutationChromosome.ofInteger(cities.size))

    // Build the evolutionary engine
    val engine = Engine.builder(fitnessFunction, genotypeFactory)
        .minimizing()
        .populationSize(500)
        .alterers(SwapMutator(0.05))
        .build()

    val generations = mutableListOf<Int>()
    val bestFitnesses = mutableListOf<Double>()
    val averageFitnesses = mutableListOf<Double>()
    val worstFitnesses = mutableListOf<Double>()


    // Run the evolution and get the best genotype
    val bestGenotype = engine.stream()
        .peek { generation ->
            val generationNumber = generation.generation()
            val fitnessValues = generation.population().map { it.fitness() }

            val bestFitness = fitnessValues.minOrNull()?.toRoundedBigDecimal(3)  ?: Double.MAX_VALUE
            val averageFitness = fitnessValues.average().toRoundedBigDecimal(3)
            val worstFitness = fitnessValues.maxOrNull()?.toRoundedBigDecimal(3) ?: Double.MIN_VALUE


            // Dodanie danych do list
            generations.add(generationNumber.toInt())
            bestFitnesses.add(bestFitness)
            worstFitnesses.add(worstFitness)
            averageFitnesses.add(averageFitness)

            println("Generation: $generationNumber, Best: $bestFitness, Average: $averageFitness, Worst: $worstFitness")
        }
        .limit(200)
        .collect(EvolutionResult.toBestGenotype())

    showChart(generations, bestFitnesses, worstFitnesses, averageFitnesses)

    // Extract and print the best route
    val bestOrder = (bestGenotype.chromosome() as PermutationChromosome<Int>)
        .stream().map { it.allele() }.toList()
    val bestRoute = bestOrder.map { cities[it] }
    println("Best Route: $bestRoute")
    println("Distance: ${totalDistance(bestRoute)}")
}

fun showChart(
    generations: List<Int>,
    bestFitness: List<Double>,
    worstFitness: List<Double>,
    averageFitness: List<Double>
) {
    val chart = XYChartBuilder()
        .width(800)
        .height(600)
        .title("Fitness Progress")
        .xAxisTitle("Generation")
        .yAxisTitle("Fitness (Distance)")
        .build()

    chart.addSeries("Best Fitness", generations, bestFitness).marker = SeriesMarkers.NONE
    chart.addSeries("Worst Fitness", generations, worstFitness).marker = SeriesMarkers.NONE
    chart.addSeries("Average Fitness", generations, averageFitness).marker = SeriesMarkers.NONE

    SwingWrapper(chart).displayChart()
}

data class City(val x: Double, val y: Double) {
    fun distanceTo(other: City): Double {
        return kotlin.math.sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
    }
}

fun Double.toRoundedBigDecimal(scale: Int): Double {
    return BigDecimal(this).setScale(scale, RoundingMode.HALF_UP).toDouble()
}

fun totalDistance(cities: List<City>): Double {
    var distance = 0.0
    for (i in 0 until cities.size - 1) {
        distance += cities[i].distanceTo(cities[i + 1])
    }
    distance += cities.last().distanceTo(cities.first()) // Powrót do startu
    return distance
}