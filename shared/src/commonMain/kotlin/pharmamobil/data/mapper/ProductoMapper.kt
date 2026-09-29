package pharmamobil.data.mapper
import pharmamobil.domain.model.ProductoRest
import pharmamobil.data.remote.dto.ProductoResponseDto
import pharmamobil.data.remote.dto.ProductoRequestDto
fun ProductoResponseDto.toDomain() = ProductoRest(id,nombre,precio,stock,estado,categoriaId,categoriaNombre)
fun ProductoRest.toRequest() = ProductoRequestDto(nombre,precio,stock,estado,requireNotNull(categoriaId))
