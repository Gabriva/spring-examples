package hello.controller.mapper;

import hello.dto.CompanyDto;
import hello.facade.CompanyFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mapper/companies")
public class CompanyMapperController {

    @Autowired
    private CompanyFacade companyFacade;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<CompanyDto> getAll() {
        return companyFacade.getAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody CompanyDto get(@PathVariable Long id) {
        return companyFacade.get(id);
    }
}
