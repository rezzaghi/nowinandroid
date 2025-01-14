/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.core.domain

import com.google.samples.apps.nowinandroid.core.data.repository.TopicsRepository
import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.domain.TopicSortField.NAME
import com.google.samples.apps.nowinandroid.core.domain.TopicSortField.NONE
import com.google.samples.apps.nowinandroid.core.domain.model.FollowableTopic
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * A use case which obtains a list of topics with their followed state.
 */
class GetFollowableTopicsStreamUseCase @Inject constructor(
    private val topicsRepository: TopicsRepository,
    private val userDataRepository: UserDataRepository
) {
    /**
     * Returns a list of topics with their associated followed state.
     *
     * @param followedTopicIdsStream - the set of topic ids which are currently being followed. By
     * default the followed topic ids are supplied from the user data repository, but in certain
     * scenarios, such as when creating a temporary set of followed topics, you may wish to override
     * this parameter to supply your own list of topic ids. @see ForYouViewModel for an example of
     * this.
     * @param sortBy - the field used to sort the topics. Default NONE = no sorting.
     */
    operator fun invoke(
        followedTopicIdsStream: Flow<Set<String>> =
            userDataRepository.userDataStream.map { userdata ->
                userdata.followedTopics
            },
        sortBy: TopicSortField = NONE
    ): Flow<List<FollowableTopic>> {
        return combine(
            followedTopicIdsStream,
            topicsRepository.getTopicsStream()
        ) { followedIds, topics ->
            val followedTopics = topics
                .map { topic ->
                    FollowableTopic(
                        topic = topic,
                        isFollowed = topic.id in followedIds
                    )
                }
            when (sortBy) {
                NAME -> followedTopics.sortedBy { it.topic.name }
                else -> followedTopics
            }
        }
    }
}

enum class TopicSortField {
    NONE,
    NAME,
}
