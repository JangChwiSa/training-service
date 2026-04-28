package com.jangchwisa.trainingservice.training.document.service;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionCommand;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionFeedback;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionProgress;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionResult;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionScore;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionService;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionSummary;
import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerRequest;
import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerResultResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerSummaryResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentQuestionResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentSessionDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.StartDocumentSessionResponse;
import com.jangchwisa.trainingservice.training.document.dto.SubmitDocumentAnswersRequest;
import com.jangchwisa.trainingservice.training.document.dto.SubmitDocumentAnswersResponse;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository.DocumentScoreRow;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository.DocumentQuestionAnswerRow;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository.ScoredDocumentAnswer;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.service.CreateTrainingSessionCommand;
import com.jangchwisa.trainingservice.training.session.service.SessionOwnershipValidator;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentTrainingService {

    private final DocumentTrainingRepository documentTrainingRepository;
    private final TrainingSessionService trainingSessionService;
    private final SessionOwnershipValidator sessionOwnershipValidator;
    private final TrainingCompletionService trainingCompletionService;

    @Autowired
    public DocumentTrainingService(
            DocumentTrainingRepository documentTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator,
            TrainingCompletionService trainingCompletionService
    ) {
        this.documentTrainingRepository = documentTrainingRepository;
        this.trainingSessionService = trainingSessionService;
        this.sessionOwnershipValidator = sessionOwnershipValidator;
        this.trainingCompletionService = trainingCompletionService;
    }

    public DocumentTrainingService(
            DocumentTrainingRepository documentTrainingRepository,
            TrainingSessionService trainingSessionService,
            SessionOwnershipValidator sessionOwnershipValidator
    ) {
        this(documentTrainingRepository, trainingSessionService, sessionOwnershipValidator, null);
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

    public SubmitDocumentAnswersResponse submitAnswers(
            CurrentUser currentUser,
            long sessionId,
            SubmitDocumentAnswersRequest request
    ) {
        ensureCompletionDependency();
        validateAnswerSubmission(currentUser, sessionId, request);
        List<Long> questionIds = request.answers().stream()
                .map(DocumentAnswerRequest::questionId)
                .toList();
        Map<Long, DocumentQuestionAnswerRow> questionById = documentTrainingRepository.findQuestionAnswers(questionIds)
                .stream()
                .collect(Collectors.toMap(DocumentQuestionAnswerRow::questionId, Function.identity()));
        if (questionById.size() != questionIds.size()) {
            throw new TrainingServiceException(ErrorCode.NOT_FOUND, "Document question was not found.");
        }

        List<ScoredDocumentAnswer> scoredAnswers = request.answers().stream()
                .map(answer -> ScoredDocumentAnswer.from(answer, questionById.get(answer.questionId())))
                .toList();
        int correctCount = (int) scoredAnswers.stream().filter(ScoredDocumentAnswer::correct).count();
        int totalCount = scoredAnswers.size();
        BigDecimal accuracyRate = BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
        int score = accuracyRate.setScale(0, RoundingMode.HALF_UP).intValue();

        TrainingCompletionResult completionResult = trainingCompletionService.complete(new TrainingCompletionCommand(
                currentUser.userId(),
                sessionId,
                TrainingType.DOCUMENT,
                new TrainingCompletionScore(
                        score,
                        "ACCURACY_RATE",
                        correctCount,
                        totalCount,
                        accuracyRate,
                        totalCount - correctCount,
                        null,
                        null
                ),
                new TrainingCompletionFeedback(
                        "SUMMARY",
                        "SYSTEM",
                        "문서 이해 훈련을 완료했습니다.",
                        "제출한 답변의 정답률을 기준으로 점수를 계산했습니다."
                ),
                new TrainingCompletionSummary(
                        null,
                        null,
                        null,
                        "문서 이해 훈련",
                        "문서 이해 훈련 완료",
                        correctCount,
                        totalCount,
                        accuracyRate,
                        totalCount - correctCount,
                        null,
                        null
                ),
                TrainingCompletionProgress.none(),
                () -> documentTrainingRepository.saveAnswerLogs(sessionId, scoredAnswers)
        ));

        List<DocumentAnswerResultResponse> results = scoredAnswers.stream()
                .map(answer -> new DocumentAnswerResultResponse(
                        answer.questionId(),
                        answer.correct(),
                        answer.correctAnswer(),
                        answer.explanation()
                ))
                .toList();
        return new SubmitDocumentAnswersResponse(
                completionResult.sessionId(),
                completionResult.score(),
                correctCount,
                totalCount,
                results,
                true
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

    private void ensureCompletionDependency() {
        if (trainingCompletionService == null) {
            throw new TrainingServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Document completion is not configured.");
        }
    }
}
