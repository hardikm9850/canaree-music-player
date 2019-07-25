package dev.olog.core.interactor

import dev.olog.core.entity.favorite.FavoriteStateEntity
import dev.olog.core.gateway.FavoriteGateway
import javax.inject.Inject

class UpdateFavoriteStateUseCase @Inject constructor(
    private val favoriteGateway: FavoriteGateway

) {

    operator fun invoke(param: FavoriteStateEntity) {
        return favoriteGateway.updateFavoriteState(param)
    }
}