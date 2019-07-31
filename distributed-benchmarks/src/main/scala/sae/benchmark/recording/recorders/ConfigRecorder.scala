package sae.benchmark.recording.recorders

import sae.benchmark.Benchmark
import sae.benchmark.recording.{Recorder, Transport}

import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._

/**
  * Records all AnyVal properties of a class
  *
  * @param executionId
  * @param nodeName
  * @param transport
  */
class ConfigRecorder(
						private val executionId: String,
						private val nodeName: String,
						private val transport: Transport[ConfigRecord] = null
					) extends Recorder[ConfigRecord](executionId, "config", nodeName, ConfigRecord, transport) {

	def log(subject: Benchmark): Unit = {
		val instanceMirror = currentMirror.reflect(subject)
		currentMirror.classSymbol(subject.getClass).toType.members
			.collect({ // All public getters
				case m: MethodSymbol if m.isGetter && m.isPublic => m
			})
			.filter(_.typeSignature.resultType <:< typeOf[AnyVal]) // Only getters for basic data types
			.foreach(accessor => {
				val typeName = accessor.typeSignature.resultType.toString
				val propertyName = accessor.name.toString
				val value = instanceMirror.reflectMethod(accessor).apply()

				log(new ConfigRecord(
					nodeName,
					typeName,
					propertyName,
					value.toString
				))
			})
	}

	def log(typeName: String, propertyName: String, value: String): Unit = {
		log(new ConfigRecord(nodeName, typeName, propertyName, value))
	}

}