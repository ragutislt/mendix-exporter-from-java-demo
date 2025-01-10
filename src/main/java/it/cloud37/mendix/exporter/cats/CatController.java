package it.cloud37.mendix.exporter.cats;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(consumes = "application/json", produces = "application/json")
public class CatController {

	private final CatService catService;

	public CatController(CatService catService) {
		this.catService = catService;
	}

	@GetMapping("/cat")
	public List<CatDto> allCats() {
		return catService.getCats();
	}

}
