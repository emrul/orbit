/*
 Copyright (C) 2015 - 2019 Electronic Arts Inc.  All rights reserved.
 This file is part of the Orbit Project <https://www.orbit.cloud>.
 See license in LICENSE.
 */

package orbit.server.pipeline

import kotlinx.coroutines.isActive
import orbit.server.pipeline.step.PipelineStep
import orbit.shared.net.Message
import java.util.concurrent.CancellationException
import kotlin.coroutines.coroutineContext

class PipelineContext(
    private val pipelineSteps: Array<PipelineStep>,
    startAtEnd: Boolean,
    private val pipeline: Pipeline
) {
    private val pipelineSize = pipelineSteps.size
    private var pointer = if (startAtEnd) pipelineSize else -1

    suspend fun nextInbound(msg: Message) {
        if (!coroutineContext.isActive) throw CancellationException()
        check(--pointer >= 0) { "Beginning of pipeline encountered." }
        val pipelineStep = pipelineSteps[pointer]
        pipelineStep.onInbound(this, msg)

    }

    suspend fun nextOutbound(msg: Message) {
        if (!coroutineContext.isActive) throw CancellationException()
        check(++pointer < pipelineSize) { "End of pipeline encountered." }
        val pipelineStep = pipelineSteps[pointer]
        pipelineStep.onOutbound(this, msg)
    }

    fun newInbound(msg: Message) =
        pipeline.pushInbound(msg)

    fun newOutbound(msg: Message) =
        pipeline.pushOutbound(msg)
}