package com.vcheck.demo.dev.ported

import java.lang.IllegalArgumentException

val allLivenessActions = arrayOf(
    "Liveness",
    "LivenessEvent",
    "LivenessFailureReason",
    "LivenessError",
    "AlreadyFinalizedError",
)

data class LivenessError(val exception: Exception)

data class AlreadyFinalizedError(val error: LivenessError)

data class ChallengeFrameErrorExceeded(val exception: Exception)

data class EndLiveness(val livenessError: LivenessError)

enum class LivenessEvent {
    START,
    NEXT_GESTURE,
    FAILURE,
    SUCCESS,
    NO_FACE,
    NEW_FACE,
    MULTIPLE_FACES
}

fun livenessEventToStrCode(livenessEvent: LivenessEvent): String {
    return livenessEvent.name.lowercase()
}

enum class LivenessFailureReason {
    FACE_NOT_FOUND,
    MULTIPLE_FACES,
    TIMEOUT,
    FAST_MOVEMENT,
    TOO_DARK
}

fun livenessFailureReasonToStrCode(livenessFailureReason: LivenessFailureReason): String {
    return livenessFailureReason.name.lowercase()
}



class Liveness(val milestones: List<GestureMilestone>,
               val minDetectionConfidence: Double = 0.5,
               val miTrackingConfidence: Double = 0.5,
                val maxNoFaceFrames: Int? = null,
                val maxMultipleFacesFrames: Int? = null,
                val timeout: Int? = null,
                val maxAnglesDiff: Int? = null,
                val darknessThreshold: Int? = null,
                val maxDarkFrames: Int? = null,
                val partialFaceOffset: Int? = null,
                vararg kwargs: Any) {

    //AsyncIOEventEmitter ==> mb RxKotlin emitter

    init {

        if (milestones.isEmpty()) {
            throw IllegalArgumentException("At least one milestone is required")
        }


    }

}

//TODO: port contents and check types

//    def __init__(
//        ):
//        //super().__init__(**kwargs)
//
//
//        self._face_mesh = FaceMesh(
//        min_detection_confidence=min_detection_confidence,
//        min_tracking_confidence=min_tracking_confidence,
//        max_num_faces=2,
//        )
//
//        self.milestones = milestones
//        self._stage = 0
//
//        self._no_face_frames_count = 0
//        self._max_no_face_frames = max_no_face_frames
//
//        self._multiple_faces_frames_count = 0
//        self._max_multiple_faces_frames = max_multiple_faces_frames
//
//        self._finalized = False
//        self._result = None
//
//        self._started = False
//        self._start_time = None
//
//        self._state = None
//
//        self.faces = []
//        self._previous_face = None
//
//        self._timeout = timeout
//
//        if max_angles_diff is not None and len(max_angles_diff) != 3:
//        raise ValueError("max_angles_diff must have 3 items")
//
//        self._max_angles_diff = max_angles_diff
//
//        self._darkness_threshold = darkness_threshold
//        self._max_dark_frames = max_dark_frames
//        self._dark_frames_count = 0
//
//        self._partial_face_offset = partial_face_offset
//
//        @property
//        def is_started(self):
//        return self._started
//
//        @property
//        def is_finalized(self):
//        return self._finalized
//
//        @property
//        def result(self):
//        return self._result
//
//        @property
//        def current_stage(self):
//        return self._stage
//
//        def __enter__(self):
//        return self
//
//        def __exit__(self, *exc_args):
//        self.close()
//
//        def close(self):
//        self._face_mesh.close()
//
//        def _get_face(self, image):
//        face = Face.from_image(
//        self._face_mesh, image, partial_face_offset=self._partial_face_offset
//        )
//
//        self.faces.append(face)
//
//        return face
//
//        @property
//        def current_milestone(self):
//        return self.milestones[self._stage]
//
//        def is_last_milestone(self):
//        return self._stage == len(self.milestones) - 1
//
//        def next_milestone(self):
//        self._stage += 1
//
//        def _is_timed_out(self):
//        if self._timeout is None or not self._started or self._start_time is None:
//        return False
//
//        return (time.time() - self._start_time) * 1e3 > self._timeout
//
//        def process_image(self, image):
//        if self.is_finalized:
//        raise AlreadyFinalizedError()
//
//        if not self.is_started:
//        self._start()
//
//        if self._is_timed_out():
//        self._fail(LivenessFailureReason.TIMEOUT)
//        return
//
//        try:
//        face = self._get_face(image)
//
//        self.emit(LivenessEvent.NEW_FACE, face)
//
//        self._after_new_face()
//
//        if self.is_finalized:
//        return
//
//        if self.current_milestone.is_met(face):
//
//        if self.is_last_milestone():
//        self._success()
//        else:
//        self.next_milestone()
//        self.emit(LivenessEvent.NEXT_GESTURE)
//
//        except NoFaceError:
//        self._no_face_frames_count += 1
//
//        if (
//        self._max_no_face_frames is not None
//        and self._no_face_frames_count >= self._max_no_face_frames
//        ):
//        self._fail(LivenessFailureReason.FACE_NOT_FOUND)
//        else:
//        self._set_state(LivenessEvent.NO_FACE)
//        except MultipleFacesError:
//        self._multiple_faces_frames_count += 1
//
//        if (
//        self._max_multiple_faces_frames is not None
//        and self._multiple_faces_frames_count >= self._max_multiple_faces_frames
//        ):
//        self._fail(LivenessFailureReason.MULTIPLE_FACES)
//        else:
//        self._set_state(LivenessEvent.MULTIPLE_FACES)
//
//        def _success(self):
//        self._finalized = True
//        self._result = True
//
//        self.emit(LivenessEvent.SUCCESS)
//
//        def _fail(self, reason):
//        self._finalized = True
//        self._result = False
//        self._reason = reason
//
//        self.emit(LivenessEvent.FAILURE, reason)
//
//        def _start(self):
//        self._started = True
//        self._start_time = time.time()
//
//        self.emit(LivenessEvent.START)
//
//        def _set_state(self, event):
//        if self._state != event:
//        self._state = event
//
//        if self._state is not None:
//        self.emit(event)
//
//        def _after_new_face(self):
//        if self._darkness_threshold is not None and self._max_dark_frames is not None:
//        face_rect = self.faces[-1].face_rect
//
//        if face_rect is not None:
//        mean_brightness = np.mean((cv.cvtColor(face_rect, cv.COLOR_BGR2GRAY)))
//
//        if mean_brightness <= self._darkness_threshold:
//        self._dark_frames_count += 1
//
//        if self._dark_frames_count >= self._max_dark_frames:
//        self._fail(LivenessFailureReason.TOO_DARK)
//        return
//
//        if self._max_angles_diff is not None and len(self.faces) > 1:
//        if not np.all(
//        np.array(self._max_angles_diff)
//        > np.abs(self.faces[-1].euler_angles - self.faces[-2].euler_angles)
//        ):
//        self._fail(LivenessFailureReason.FAST_MOVEMENT)
//        return

