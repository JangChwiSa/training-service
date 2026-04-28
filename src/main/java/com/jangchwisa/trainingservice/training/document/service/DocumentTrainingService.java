package com.jangchwisa.trainingservice.training.document.service;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerRequest;
import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerSummaryResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentQuestionResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentSessionDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.StartDocumentSessionResponse;
import com.jangchwisa.trainingservice.training.document.dto.SubmitDocumentAnswersRequest;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository.DocumentScoreRow;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.service.CreateTrainingSessionCommand;
import com.jangchwisa.trainingservice.training.session.service.SessionOwnershipValidator;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentTrainingService {

    private final DocumentTrainingRepository documentTrainingRepository;
    private final TrainingSessionService trainingSessionService;
    private final SessionOwnershipValidator sessionOwnershipValidator;

    public DocumentTrainingService(
            DocumentTrainingRepository documentTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator
    ) {
        this.documentTrainingRepository = documentTrainingRepository;
        this.trainingSessionService = trainingSessionService;
        this.sessionOwnershipValidator = sessionOwnershipValidator;
    }

    @Transactional
    public StartDocumentSessionResponse startSession(CurrentUser currentUser) {
        List<DocumentQuestionResponse> questions = documentTrainingRepository.findActiveQuestions();
        if (questions.isEmpty()) {
            throw new TrainingServiceException(ErrorCode.NOT_FOUND, "Document question was not found.");
        }

        TrainingSession session = trainingSessionService.createSession(
                currentUser,
                new CreateTrainingSessionCommand(currentUser.userId(), TrainingType.DOCUMENT, null, null)
        );

        return new StartDocumentSessionResponse(session.sessionId(), questions);
    }

    @Transactional(readOnly = true)
    public void validateAnswerSubmission(
            CurrentUser currentUser,
            long sessionId,
            SubmitDocumentAnswersRequest request
    ) {
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);
        validateUniqueQuestionIds(request.answers());
    }

    @Transactional(readOnly = true)
    public DocumentSessionDetailResponse getSessionDetail(CurrentUser currentUser, long sessionId) {
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);

        DocumentScoreRow score = documentTrainingRepository.findScore(sessionId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Document training score was not found."));

        return new DocumentSessionDetailResponse(
                sessionId,
                score.score(),
                new DocumentAnswerSummaryResponse(score.correctCount(), score.totalCount()),
                documentTrainingRepository.findAnswerLogs(sessionId)
        );
    }

    private void validateUniqueQuestionIds(List<DocumentAnswerRequest> answers) {
        Set<Long> questionIds = new HashSet<>();
        for (DocumentAnswerRequest answer : answers) {
            if (!questionIds.add(answer.questionId())) {
                throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Duplicate document answer question id.");
            }
        }
    }
}
